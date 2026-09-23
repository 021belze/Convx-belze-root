/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.vivimusic.updater


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import timber.log.Timber
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.convx.music.BuildConfig
import com.convx.music.R
import coil3.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.convx.music.vivimusic.updater.downloadmanager.UpdateDownloadWorker
import com.convx.music.vivimusic.updater.downloadmanager.DownloadNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import com.convx.music.ui.component.ChangelogItem
import com.convx.music.ui.component.leadingItemShape
import com.convx.music.ui.component.middleItemShape
import com.convx.music.ui.component.endItemShape
import com.convx.music.ui.component.detachedItemShape
import com.convx.music.ui.component.AnimatedActionButton
import com.convx.music.ui.component.ExpressiveIconButton
import com.convx.music.ui.component.ErrorSnackbar
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.style.TextDecoration

data class ChangelogSection(val title: String, val items: List<String>)

sealed class ViviUpdateStatus {
    object Idle : ViviUpdateStatus()
    object Checking : ViviUpdateStatus()
    data class Available(
        val version: String,
        val changelog: List<ChangelogSection>,
        val size: String,
        val releaseDate: String,
        val description: String?,
        val imageUrl: String?,
        val apkUrl: String?
    ) : ViviUpdateStatus()

    data class NoUpdate(val version: String) : ViviUpdateStatus()
    data class Error(val message: String) : ViviUpdateStatus()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<ViviUpdateStatus>(ViviUpdateStatus.NoUpdate(BuildConfig.VERSION_NAME)) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloadComplete by remember { mutableStateOf(false) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentVersion = BuildConfig.VERSION_NAME
    val autoUpdateCheckEnabled = getAutoUpdateCheckSetting(context)

    LaunchedEffect(Unit) {
        DownloadNotificationManager.initialize(context)
    }

    // Observe WorkManager for download progress
    LaunchedEffect(Unit) {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData("update_download")
            .observeForever { workInfos ->
                val workInfo = workInfos?.firstOrNull() ?: return@observeForever

                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        isDownloading = true
                        downloadProgress = workInfo.progress.getFloat("progress", 0f)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        isDownloading = false
                        isDownloadComplete = true
                        val filePath = workInfo.outputData.getString("file_path")
                        if (filePath != null) {
                            downloadedFile = File(filePath)
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        isDownloading = false
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.download_failed))
                        }
                    }
                    WorkInfo.State.CANCELLED -> {
                        isDownloading = false
                        downloadProgress = 0f
                    }
                    else -> {}
                }
            }
    }

    // Check if downloaded file still exists
    LaunchedEffect(isDownloadComplete, downloadedFile) {
        if (isDownloadComplete && downloadedFile != null) {
            if (!downloadedFile!!.exists()) {
                isDownloadComplete = false
                downloadedFile = null
                downloadProgress = 0f
            }
        }
    }

    fun triggerUpdateCheck() {
        status = ViviUpdateStatus.Checking
        scope.launch {
            // Add a small delay for visual feedback as in Med
            delay(1000L)
            checkForUpdate(
                context = context,
                onSuccess = { tag, isAvailable, changelog, size, date, description, imageUrl, apkUrl ->
                    saveLastCheckedTime(context, LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a")))
                    saveUpdateAvailableState(context, isAvailable)
                    status = if (isAvailable) {
                        ViviUpdateStatus.Available(
                            version = tag,
                            changelog = changelog,
                            size = size,
                            releaseDate = date,
                            description = description,
                            imageUrl = imageUrl,
                            apkUrl = apkUrl
                        )
                    } else {
                        ViviUpdateStatus.NoUpdate(tag)
                    }
                },
                onError = {
                    status = ViviUpdateStatus.Error(context.getString(R.string.cant_check_updates))
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (autoUpdateCheckEnabled) {
            triggerUpdateCheck()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    val titleText = if (status is ViviUpdateStatus.Available) {
                        buildAnnotatedString {
                            append(stringResource(R.string.new_update) + " ")
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append((status as ViviUpdateStatus.Available).version)
                            }
                        }
                    } else {
                        AnnotatedString(stringResource(R.string.settings_check_updates_title))
                    }
                    Text(text = titleText, maxLines = 1)
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        ExpressiveIconButton(
                            onClick = { navController.navigateUp() },
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.cancel),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val currentStatus = status) {
                        is ViviUpdateStatus.Idle, is ViviUpdateStatus.Checking, is ViviUpdateStatus.NoUpdate, is ViviUpdateStatus.Error -> {
                            AnimatedActionButton(
                                text = stringResource(R.string.check_for_update),
                                onClick = { triggerUpdateCheck() },
                                enabled = currentStatus !is ViviUpdateStatus.Checking && !isDownloading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is ViviUpdateStatus.Available -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnimatedActionButton(
                                    text = stringResource(R.string.later),
                                    onClick = { navController.navigateUp() },
                                    modifier = Modifier.weight(1f),
                                    isOutlined = true,
                                    enabled = !isDownloading
                                )
                                AnimatedActionButton(
                                    text = if (isDownloading) "${(downloadProgress * 100).toInt()}%" else if (isDownloadComplete) stringResource(R.string.install) else stringResource(R.string.update_available),
                                    onClick = {
                                        if (isDownloadComplete) {
                                            val file = downloadedFile
                                            if (file == null || !file.exists()) {
                                                isDownloadComplete = false
                                                downloadedFile = null
                                                downloadProgress = 0f
                                                return@AnimatedActionButton
                                            }
                                            file.let { f ->
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    if (!context.packageManager.canRequestPackageInstalls()) {
                                                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                                            data = Uri.parse("package:${context.packageName}")
                                                        }
                                                        context.startActivity(intent)
                                                        return@let
                                                    }
                                                }
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", file)
                                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                ContextCompat.startActivity(context, installIntent, null)
                                            }
                                        } else {
                                            val urlToDownload = currentStatus.apkUrl ?: "https://github.com/$GITHUB_REPO/releases/download/${currentStatus.version}/convx.apk"
                                            val downloadRequest = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
                                                .setInputData(workDataOf("apk_url" to urlToDownload, "version" to currentStatus.version, "file_size" to currentStatus.size))
                                                .addTag("update_download")
                                                .build()
                                            WorkManager.getInstance(context).enqueueUniqueWork("update_download", ExistingWorkPolicy.REPLACE, downloadRequest)
                                            isDownloading = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isDownloading || isDownloadComplete
                                )
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { ErrorSnackbar(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .widthIn(max = 700.dp)
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    val contentModifier = if (status is ViviUpdateStatus.Available) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.fillParentMaxSize()
                    }

                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        when (val currentStatus = status) {
                            is ViviUpdateStatus.Checking -> {
                                androidx.compose.material3.ContainedLoadingIndicator(
                                    modifier = Modifier.size(64.dp)
                                )
                            }

                            is ViviUpdateStatus.NoUpdate -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.deployed_app_update),
                                        contentDescription = null,
                                        modifier = Modifier.size(120.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = stringResource(R.string.on_latest_version),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.current_version_v, currentStatus.version),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            is ViviUpdateStatus.Error -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.error),
                                        contentDescription = null,
                                        modifier = Modifier.size(120.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = currentStatus.message,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            is ViviUpdateStatus.Available -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = stringResource(R.string.release_date_v, currentStatus.releaseDate),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (currentStatus.size.isNotBlank()) {
                                        Text(
                                            text = stringResource(R.string.update_size_v, currentStatus.size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    if (!currentStatus.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = currentStatus.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(24.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                    if (!currentStatus.description.isNullOrBlank()) {
                                        val urls = currentStatus.description.extractUrls()
                                        val annotatedText = buildAnnotatedString {
                                            append(currentStatus.description.trim())
                                            urls.forEach { (range, url) ->
                                                addStringAnnotation("URL", url, range.first, range.last + 1)
                                                addStyle(
                                                    SpanStyle(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textDecoration = TextDecoration.Underline
                                                    ),
                                                    range.first,
                                                    range.last + 1
                                                )
                                            }
                                        }

                                        ClickableText(
                                            text = annotatedText,
                                            onClick = { offset ->
                                                annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                                                    ContextCompat.startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(it.item)), null)
                                                }
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 20.sp
                                            ),
                                            modifier = Modifier.padding(bottom = 24.dp)
                                        )
                                    }
                                    if (isDownloading) {
                                        if (downloadProgress > 0f) {
                                            androidx.compose.material3.LinearProgressIndicator(
                                                progress = downloadProgress,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        } else {
                                            androidx.compose.material3.LinearProgressIndicator(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                    currentStatus.changelog.forEach { section ->
                                        if (section.title.isNotBlank()) {
                                            Text(
                                                text = section.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                                            )
                                        }
                                        section.items.forEachIndexed { index, item ->
                                            val shape = when {
                                                section.items.size == 1 -> com.convx.music.ui.component.detachedItemShape()
                                                index == 0 -> com.convx.music.ui.component.leadingItemShape()
                                                index == section.items.size - 1 -> com.convx.music.ui.component.endItemShape()
                                                else -> com.convx.music.ui.component.middleItemShape()
                                            }
                                            com.convx.music.ui.component.ChangelogItem(
                                                text = item,
                                                shape = shape,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}


// Utility functions for SharedPreferences uses now view model
const val PREFS_NAME = "settings"
const val KEY_AUTO_UPDATE_CHECK = "auto_update_check"
const val KEY_LAST_CHECKED_TIME = "last_checked_time"
const val KEY_BETA_UPDATES = "beta_updates"
const val KEY_UPDATE_AVAILABLE = "update_available"

fun getUpdateAvailableState(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_UPDATE_AVAILABLE, false)
}

fun saveUpdateAvailableState(context: Context, available: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_UPDATE_AVAILABLE, available).apply()
}

fun getAutoUpdateCheckSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_AUTO_UPDATE_CHECK, false)
}

fun saveAutoUpdateCheckSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_AUTO_UPDATE_CHECK, enabled).apply()
}

const val KEY_UPDATE_NOTIFICATIONS = "update_notifications"

fun getUpdateNotificationsSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_UPDATE_NOTIFICATIONS, false)
}

fun saveUpdateNotificationsSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_UPDATE_NOTIFICATIONS, enabled).apply()
}

fun saveLastCheckedTime(context: Context, timestamp: String) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putString(KEY_LAST_CHECKED_TIME, timestamp).apply()
}

fun getLastCheckedTime(context: Context): String {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getString(KEY_LAST_CHECKED_TIME, "") ?: ""
}

fun getBetaUpdatesSetting(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(KEY_BETA_UPDATES, false)
}

fun saveBetaUpdatesSetting(context: Context, enabled: Boolean) {
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean(KEY_BETA_UPDATES, enabled).apply()
}

private fun formatGitHubDate(githubDate: String): String = try {
    val githubFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a")
    val dateTime = LocalDateTime.parse(githubDate, githubFormatter)
    dateTime.format(displayFormatter)
} catch (e: Exception) {
    githubDate
}

const val GITHUB_REPO_OWNER = "021belze"
const val GITHUB_REPO_NAME = "Convx-belze-root"
const val GITHUB_REPO = "$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME"

data class ParsedVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val revision: Int,
    val isBeta: Boolean,
)

fun parseVersion(versionStr: String): ParsedVersion {
    val isBeta = versionStr.startsWith("b", ignoreCase = true)
    val clean = versionStr.trim().removePrefix("v").removePrefix("V").removePrefix("b").removePrefix("B")

    // Split semver from suffixes: e.g. "1.5.2-R8-Optimized" or "1.5.2 R10-optimized" -> ["1.5.2", "R8", "Optimized"]
    val parts = clean.split(Regex("""[-_\s]+"""))
    val semverParts = parts.firstOrNull()?.split(".")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    val major = semverParts.getOrElse(0) { 0 }
    val minor = semverParts.getOrElse(1) { 0 }
    val patch = semverParts.getOrElse(2) { 0 }

    // Check revision in any part, e.g. "r10", "R8", "rev2"
    var rev = 0
    for (part in parts) {
        val rMatch = Regex("""[rR](\d+)""").find(part)
        if (rMatch != null) {
            rev = rMatch.groupValues[1].toIntOrNull() ?: 0
            break
        }
    }
    // Also check if 4th component exists in dot notation: 1.5.2.1
    if (rev == 0 && semverParts.size > 3) {
        rev = semverParts[3]
    }

    return ParsedVersion(major, minor, patch, rev, isBeta)
}

// Robust version comparison: returns true if latestVersion > currentVersion
fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
    val latest = parseVersion(latestVersion)
    val current = parseVersion(currentVersion)

    if (latest.major != current.major) return latest.major > current.major
    if (latest.minor != current.minor) return latest.minor > current.minor
    if (latest.patch != current.patch) return latest.patch > current.patch
    if (latest.revision != current.revision) return latest.revision > current.revision

    // If numerical components are equal, stable release is newer than beta
    if (current.isBeta && !latest.isBeta) return true

    return false
}

// Clean and format markdown release notes into ChangelogSection items
fun parseMarkdownChangelog(body: String, defaultTitle: String): List<ChangelogSection> {
    if (body.isBlank()) return emptyList()
    val lines = body.lines()
    val sections = mutableListOf<ChangelogSection>()
    var currentTitle = defaultTitle
    val currentItems = mutableListOf<String>()

    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isBlank() || line.startsWith("---") || line.startsWith("<") || line.startsWith("|")) {
            continue
        }
        if (line.startsWith("#")) {
            // New section header
            if (currentItems.isNotEmpty()) {
                sections.add(ChangelogSection(currentTitle, currentItems.toList()))
                currentItems.clear()
            }
            currentTitle = line.trimStart('#').trim()
        } else if (line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ")) {
            val item = line.substring(2).trim()
            if (item.isNotBlank()) {
                currentItems.add(item)
            }
        } else if (line.isNotEmpty()) {
            currentItems.add(line)
        }
    }

    if (currentItems.isNotEmpty()) {
        sections.add(ChangelogSection(currentTitle, currentItems.toList()))
    }
    return if (sections.isNotEmpty()) sections else listOf(ChangelogSection(defaultTitle, listOf(body.trim())))
}

// Real download size of the nightly zip, from the workflow run's artifact listing.
// Returns "" on failure so the UI simply omits the size rather than lying about it.
private fun fetchNightlyArtifactSize(runId: Long): String = try {
    val artifactsUrl =
        URL("https://api.github.com/repos/$GITHUB_REPO/actions/runs/$runId/artifacts")
    val artifacts = JSONObject(artifactsUrl.openStream().bufferedReader().use { it.readText() })
        .optJSONArray("artifacts")
    val bytes = (0 until (artifacts?.length() ?: 0))
        .map { artifacts!!.getJSONObject(it) }
        .firstOrNull { it.optString("name") == "convx-gms-nightly" }
        ?.optLong("size_in_bytes") ?: 0L
    if (bytes > 0) String.format("%.1f", bytes / (1024.0 * 1024.0)) else ""
} catch (e: Exception) {
    Timber.tag("UpdateCheck").e(e, "Error fetching nightly artifact size: ${e.message}")
    ""
}

// Fetches ALL releases, finds the latest version > current, and returns its info
suspend fun checkForUpdate(
    context: Context,
    onSuccess: (tag: String, isAvailable: Boolean, changelog: List<ChangelogSection>, size: String, date: String, description: String?, imageUrl: String?, apkUrl: String?) -> Unit,
    onError: () -> Unit,
) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_REPO/releases")
            val json = url.openStream().bufferedReader().use { it.readText() }
            val releases = JSONArray(json)
            
            val currentVersion = BuildConfig.VERSION_NAME
            val betaEnabled = getBetaUpdatesSetting(context)

            var isNightlyUpdate = false
            var nightlyRunObject: JSONObject? = null

            if (betaEnabled) {
                try {
                    val nightlyUrl = URL("https://api.github.com/repos/$GITHUB_REPO/actions/workflows/nightly.yml/runs?status=success&per_page=1")
                    val nightlyJson = nightlyUrl.openStream().bufferedReader().use { it.readText() }
                    val nightlyData = JSONObject(nightlyJson)
                    val runs = nightlyData.optJSONArray("workflow_runs")
                    if (runs != null && runs.length() > 0) {
                        val firstRun = runs.getJSONObject(0)
                        val runNumber = firstRun.getInt("run_number")

                        if (runNumber > BuildConfig.NIGHTLY_RUN) {
                            isNightlyUpdate = true
                            nightlyRunObject = firstRun
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag("UpdateCheck").e(e, "Error checking nightly updates: ${e.message}")
                }
            }

            if (isNightlyUpdate && nightlyRunObject != null) {
                val runNumber = nightlyRunObject.getInt("run_number")
                val runUpdatedAt = nightlyRunObject.getString("updated_at")
                val displayTag = "nightly-r$runNumber"
                
                val changelogList = mutableListOf<ChangelogSection>()
                val headCommit = nightlyRunObject.optJSONObject("head_commit")
                val commitMessage = headCommit?.optString("message") ?: "New features and bug fixes"
                val subjectLine = commitMessage.lineSequence().firstOrNull { it.isNotBlank() } ?: commitMessage
                changelogList.add(ChangelogSection(context.getString(R.string.changelog), listOf(subjectLine)))
                
                val formattedReleaseDate = formatGitHubDate(runUpdatedAt)
                val apkDownloadUrl = "https://nightly.link/$GITHUB_REPO/workflows/nightly.yml/main/convx-gms-nightly.zip"
                val apkSize = fetchNightlyArtifactSize(nightlyRunObject.getLong("id"))

                withContext(Dispatchers.Main) {
                    onSuccess(displayTag, true, changelogList, apkSize, formattedReleaseDate, "Bleeding-edge nightly build from main branch.", null, apkDownloadUrl)
                }
                return@withContext
            }

            var bestStableRelease: JSONObject? = null
            var bestOverallRelease: JSONObject? = null

            for (i in 0 until releases.length()) {
                val release = releases.getJSONObject(i)
                val tagName = release.getString("tag_name")
                val isBeta = tagName.startsWith("b", ignoreCase = true)
                
                // Track best stable
                if (!isBeta) {
                    if (bestStableRelease == null || isNewerVersion(tagName, bestStableRelease.getString("tag_name"))) {
                        bestStableRelease = release
                    }
                }
                
                // Track best overall
                if (bestOverallRelease == null || isNewerVersion(tagName, bestOverallRelease.getString("tag_name"))) {
                    bestOverallRelease = release
                }
            }

            // Select the target release based on user preference
            val targetRelease = if (betaEnabled) bestOverallRelease else bestStableRelease

            if (targetRelease != null) {
                val targetTagName = targetRelease.getString("tag_name")
                val isNewer = isNewerVersion(targetTagName, currentVersion)
                
                val shouldShow = isNewer

                if (shouldShow) {
                    val tagWithPrefix = targetRelease.getString("tag_name")
                    val displayTag = tagWithPrefix

                    // FETCH CHANGELOG.JSON FROM RELEASE ASSETS OR PARSE MARKDOWN BODY
                    val changelogList = mutableListOf<ChangelogSection>()
                    var description: String? = null
                    var imageUrl: String? = null
                    try {
                        val changelogUrl =
                            URL("https://github.com/$GITHUB_REPO/releases/download/$tagWithPrefix/changelog.json")
                        val changelogJson = changelogUrl.openStream().bufferedReader().use { it.readText() }
                        val changelogData = JSONObject(changelogJson)

                        description = changelogData.optString("description").takeIf { it.isNotEmpty() }
                        imageUrl = changelogData.optString("image").takeIf { it.isNotEmpty() }

                        val changelogArray = changelogData.getJSONArray("changelog")
                        for (j in 0 until changelogArray.length()) {
                            val sectionObj = changelogArray.getJSONObject(j)
                            val title = sectionObj.getString("title")
                            val itemsArray = sectionObj.getJSONArray("items")
                            val itemsList = mutableListOf<String>()
                            for (k in 0 until itemsArray.length()) {
                                itemsList.add(itemsArray.getString(k))
                            }
                            changelogList.add(ChangelogSection(title, itemsList))
                        }
                    } catch (e: Exception) {
                        // Fallback: Parse markdown release body cleanly
                        val body = targetRelease.optString("body", context.getString(R.string.no_changelog_available))
                        val parsedSections = parseMarkdownChangelog(body, context.getString(R.string.changelog))
                        changelogList.addAll(parsedSections)
                    }

                    val publishedAt = targetRelease.getString("published_at")
                    val formattedReleaseDate = formatGitHubDate(publishedAt)
                    val assets = targetRelease.getJSONArray("assets")

                    var bestApkUrl = ""
                    var bestApkSizeInMB = ""
                    var bestMatchScore = -1
                    val targetArch = BuildConfig.ARCHITECTURE.lowercase()

                    for (j in 0 until assets.length()) {
                        val asset = assets.getJSONObject(j)
                        val assetName = asset.getString("name").lowercase()
                        if (assetName.endsWith(".apk")) {
                            val apkSizeInBytes = asset.getLong("size")
                            val sizeMB = String.format("%.1f", apkSizeInBytes / (1024.0 * 1024.0))
                            val url = asset.getString("browser_download_url")

                            var score = 0
                            if (targetArch.isNotEmpty() && (assetName.contains(targetArch) || (targetArch == "arm64" && assetName.contains("arm64-v8a")))) {
                                score = 3
                            } else if (assetName.contains("universal")) {
                                score = 2
                            } else if (assetName == "convx.apk") {
                                score = 1
                            }

                            if (score > bestMatchScore || bestApkUrl.isEmpty()) {
                                bestMatchScore = score
                                bestApkUrl = url
                                bestApkSizeInMB = sizeMB
                            }
                        }
                    }

                    if (bestApkUrl.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            onSuccess(displayTag, true, changelogList, bestApkSizeInMB, formattedReleaseDate, description, imageUrl, bestApkUrl)
                        }
                        return@withContext
                    }
                }
            }

            // No update found or APK missing
            withContext(Dispatchers.Main) {
                onSuccess(currentVersion, false, emptyList(), "", "", null, null, null)
            }
        } catch (e: Exception) {
            Timber.tag("UpdateCheck").e(e, "Error checking for updates: ${e.message}")
            withContext(Dispatchers.Main) { onError() }
        }
    }
}
fun String.extractUrls(): List<Pair<IntRange, String>> {
    val urlPattern = Pattern.compile(
        "(?:^|[\\s])((https?://|www\\.|pic\\.)[\\w-]+(\\.[\\w-]+)+([/?].*)?)"
    )
    val matcher = urlPattern.matcher(this)
    val urlList = mutableListOf<Pair<IntRange, String>>()

    while (matcher.find()) {
        val url = matcher.group(1)?.trim() ?: continue
        val range = IntRange(matcher.start(1), matcher.end(1) - 1)
        // Ensure URL has proper scheme
        val fullUrl = if (url.startsWith("http")) url else "https://$url"
        urlList.add(range to fullUrl)
    }

    return urlList
}
