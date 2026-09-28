package com.convx.music.ui.screens.settings

import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import timber.log.Timber
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.music.spotify.SpotifyAuth
import com.music.spotify.SpotifyMapper
import com.music.spotify.models.SpotifyPlaylist
import com.convx.music.LocalPlayerAwareWindowInsets
import com.convx.music.R
import com.convx.music.ui.component.DefaultDialog
import com.convx.music.ui.component.IconButton
import com.convx.music.ui.component.Material3SettingsGroup
import com.convx.music.ui.component.Material3SettingsItem
import com.convx.music.ui.menu.LoadingScreen
import com.convx.music.ui.utils.appTopBarWindowInsets
import com.convx.music.ui.utils.backToMain
import com.convx.music.utils.rememberPreference
import com.convx.music.viewmodels.SpotifyImportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: SpotifyImportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showSpotifyLogin by remember { mutableStateOf(false) }
    var showPlaylistsSheet by remember { mutableStateOf(false) }
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()

    val refreshEnabled = state.isAuthenticated && !state.isLoading
    val rotationAngle by if (state.isLoading) {
        val transition = rememberInfiniteTransition(label = "rotation")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotationAngle"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        )

        Text(
            text = stringResource(R.string.spotify),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )

        // Connection Card/Group
        Material3SettingsGroup(
            title = stringResource(R.string.spotify_account),
            items = listOf(
                if (state.isAuthenticated) {
                    Material3SettingsItem(
                        leadingContent = if (!state.accountAvatarUrl.isNullOrBlank()) {
                            {
                                AsyncImage(
                                    model = state.accountAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else null,
                        icon = if (state.accountAvatarUrl.isNullOrBlank()) painterResource(R.drawable.spotify) else null,
                        title = {
                            Text(
                                text = if (state.accountName.isNotBlank()) state.accountName
                                else stringResource(R.string.spotify_account),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        trailingContent = {
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(stringResource(R.string.action_logout))
                            }
                        },
                        onClick = {}
                    )
                } else {
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.spotify_connect)) },
                        description = { Text(stringResource(R.string.spotify_not_connected)) },
                        icon = painterResource(R.drawable.spotify),
                        onClick = { showSpotifyLogin = true }
                    )
                }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        val totalPlaylists = state.playlists.size + if (state.likedSongsCount > 0) 1 else 0
        Material3SettingsGroup(
            title = stringResource(R.string.playlists),
            items = listOf(
                Material3SettingsItem(
                    title = { Text(stringResource(R.string.spotify_select_sources)) },
                    description = {
                        Text(
                            if (state.isAuthenticated) {
                                if (totalPlaylists > 0) stringResource(R.string.spotify_available_count, totalPlaylists)
                                else stringResource(R.string.spotify_no_sources)
                            } else {
                                stringResource(R.string.spotify_not_connected)
                            }
                        )
                    },
                    icon = painterResource(R.drawable.bookmark_star_library),
                    enabled = state.isAuthenticated && totalPlaylists > 0 && !state.isLoading,
                    onClick = { showPlaylistsSheet = true }
                ),
                Material3SettingsItem(
                    title = { Text(stringResource(R.string.spotify_refresh)) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.sync),
                                contentDescription = null,
                                tint = if (!refreshEnabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        rotationZ = rotationAngle
                                    }
                            )
                        }
                    },
                    enabled = refreshEnabled,
                    onClick = { viewModel.loadSources() }
                )
            )
        )

        // Info block
        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(R.string.spotify_import_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    TopAppBar(
            windowInsets = appTopBarWindowInsets(),
        title = {},
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )

    if (showSpotifyLogin) {
        SpotifyLoginSheet(
            onDismiss = { showSpotifyLogin = false },
            onCookiesCaptured = { spDc, spKey ->
                showSpotifyLogin = false
                viewModel.connectWithCookies(spDc, spKey)
            }
        )
    }

    importProgress?.let { progress ->
        val isFinished = progress.isFinished || progress.percent >= 1f
        DefaultDialog(
            onDismiss = {
                if (isFinished) {
                    viewModel.dismissImportProgress()
                } else {
                    viewModel.cancelImport()
                }
            },
            title = {
                Text(
                    text = if (isFinished) {
                        stringResource(R.string.spotify_import_complete)
                    } else {
                        stringResource(R.string.spotify_import_in_progress)
                    }
                )
            },
            buttons = {
                if (isFinished) {
                    Button(
                        onClick = { viewModel.dismissImportProgress() },
                        shape = CircleShape
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                } else {
                    TextButton(onClick = { viewModel.cancelImport() }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.playlists) + ": " + progress.playlistName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFinished) {
                        stringResource(R.string.spotify_imported_songs_count, progress.currentSongIndex, progress.totalSongs)
                    } else {
                        stringResource(R.string.spotify_importing_songs_count, progress.currentSongIndex, progress.totalSongs)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { progress.percent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
    state.errorMessage?.let { error ->
        DefaultDialog(
            onDismiss = { viewModel.dismissError() },
            title = { Text("Error") },
            buttons = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showPlaylistsSheet) {
        SpotifyPlaylistBottomSheet(
            onDismiss = { showPlaylistsSheet = false },
            viewModel = viewModel
        )
    }
}

private const val SPOTIFY_CHROME_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotifyLoginSheet(
    onDismiss: () -> Unit,
    onCookiesCaptured: (spDc: String, spKey: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var webView by remember { mutableStateOf<WebView?>(null) }
    var captured by remember { mutableStateOf(false) }
    var isLoadingPage by remember { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var manualCookieText by rememberSaveable { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
            webView?.destroy()
            webView = null
        }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.spotify_login_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (selectedTab == 0) {
                            stringResource(R.string.spotify_waiting_for_login)
                        } else {
                            "Paste your sp_dc cookie to connect"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (selectedTab == 0) {
                    IconButton(
                        onClick = {
                            webView?.let { wv ->
                                isLoadingPage = true
                                wv.clearCache(true)
                                wv.reload()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.sync),
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("In-App Browser") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Manual Cookie") }
                )
            }

            if (selectedTab == 0) {
                if (isLoadingPage) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(MaterialTheme.shapes.large),
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.parseColor("#121212"))

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                allowContentAccess = true
                                allowFileAccess = false
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(false)
                                userAgentString = SPOTIFY_CHROME_USER_AGENT
                            }

                            if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                                WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, emptySet())
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    if (newProgress >= 85) {
                                        isLoadingPage = false
                                    }
                                }

                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    Timber.d("Spotify WebView JS: [${consoleMessage?.messageLevel()}] ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                                    return super.onConsoleMessage(consoleMessage)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                private fun captureCookies(url: String?): Boolean {
                                    if (captured) return true
                                    cookieManager.flush()
                                    val spotifyCookies = cookieManager.getCookie("https://spotify.com") ?: ""
                                    val openCookies = cookieManager.getCookie("https://open.spotify.com") ?: ""
                                    val accountsCookies = cookieManager.getCookie("https://accounts.spotify.com") ?: ""
                                    val allCookiesStr = "$spotifyCookies; $openCookies; $accountsCookies"

                                    val cookies = allCookiesStr.split(";").associate {
                                        val parts = it.split("=")
                                        val key = parts.firstOrNull()?.trim().orEmpty()
                                        val valStr = parts.drop(1).joinToString("=").trim()
                                        key to valStr
                                    }
                                    val spDc = cookies["sp_dc"].orEmpty()
                                    if (spDc.isBlank()) return false
                                    captured = true
                                    onCookiesCaptured(spDc, cookies["sp_key"].orEmpty())
                                    return true
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest,
                                ): Boolean {
                                    val url = request.url?.toString().orEmpty()
                                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                        return true
                                    }
                                    return captureCookies(url)
                                }

                                override fun onPageStarted(
                                    view: WebView,
                                    url: String?,
                                    favicon: android.graphics.Bitmap?,
                                ) {
                                    isLoadingPage = true
                                    captureCookies(url)
                                }

                                override fun onPageFinished(view: WebView, url: String?) {
                                    isLoadingPage = false
                                    captureCookies(url)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    Timber.e("Spotify WebView onReceivedError: ${error?.errorCode} ${error?.description} for ${request?.url}")
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: WebResourceResponse?
                                ) {
                                    Timber.e("Spotify WebView onReceivedHttpError: ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase} for ${request?.url}")
                                }
                            }
                            webView = this
                            loadUrl(SpotifyAuth.LOGIN_URL)
                        }
                    },
                    update = { view ->
                        webView = view
                    },
                )
            } else {
                // Manual Cookie Input Fallback
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "If in-app login is blocked by your network or browser security, you can log in once to open.spotify.com in your normal browser, copy the 'sp_dc' cookie, and paste it below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = manualCookieText,
                        onValueChange = { manualCookieText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("sp_dc Cookie Value") },
                        placeholder = { Text("AQ... or similar cookie value") },
                        singleLine = false,
                        maxLines = 4,
                        trailingIcon = {
                            if (manualCookieText.isNotEmpty()) {
                                IconButton(onClick = { manualCookieText = "" }) {
                                    Icon(painterResource(R.drawable.close), contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                clipboardManager.getText()?.text?.let { text ->
                                    val cleaned = text.trim()
                                    // Handle cases where user copied "sp_dc=AQ..."
                                    val token = if (cleaned.contains("sp_dc=")) {
                                        cleaned.substringAfter("sp_dc=").substringBefore(";")
                                    } else {
                                        cleaned
                                    }
                                    manualCookieText = token
                                }
                            }
                        ) {
                            Text("Paste from Clipboard")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = manualCookieText.isNotBlank(),
                            onClick = {
                                val cleaned = manualCookieText.trim()
                                val token = if (cleaned.contains("sp_dc=")) {
                                    cleaned.substringAfter("sp_dc=").substringBefore(";")
                                } else {
                                    cleaned
                                }
                                if (token.isNotBlank()) {
                                    onCookiesCaptured(token, "")
                                }
                            }
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}
