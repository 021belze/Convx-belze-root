package com.convx.music.vivimusic.updater.downloadmanager

import android.content.Context
import android.os.Build
import android.os.Environment
import com.convx.music.R
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class CustomDownloadManager {
    private var downloadJob: Job? = null
    private var isPaused = false

    fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit,
        onDownloadComplete: (File) -> Unit,
        onError: (String) -> Unit,
    ) {
        downloadJob?.cancel()
        isPaused = false

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                var currentUrl = apkUrl
                lateinit var conn: HttpURLConnection
                var redirectCount = 0
                val maxRedirects = 5

                while (true) {
                    val url = URL(currentUrl)
                    val httpConn = url.openConnection() as HttpURLConnection
                    httpConn.instanceFollowRedirects = true
                    httpConn.requestMethod = "GET"
                    httpConn.connectTimeout = 15000
                    httpConn.readTimeout = 20000
                    httpConn.setRequestProperty("User-Agent", "Convx-Updater")
                    httpConn.connect()

                    val responseCode = httpConn.responseCode
                    if (responseCode in 301..308) {
                        val location = httpConn.getHeaderField("Location")
                        httpConn.disconnect()
                        if (!location.isNullOrEmpty() && redirectCount < maxRedirects) {
                            currentUrl = location
                            redirectCount++
                            continue
                        }
                    }
                    conn = httpConn
                    break
                }

                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        onError("Server returned HTTP ${conn.responseCode}")
                    }
                    conn.disconnect()
                    return@launch
                }

                val fileLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    conn.contentLengthLong.takeIf { it > 0 } ?: conn.contentLength.toLong()
                } else {
                    conn.contentLength.toLong()
                }

                // Create download directory
                val downloadDir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "vivi_updates"
                )
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }

                val outputFile = File(downloadDir, "vivi.apk")
                val bufferSize = 64 * 1024
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                var lastProgressPercent = -1
                var lastReportTime = 0L

                BufferedInputStream(conn.inputStream, bufferSize).use { bis ->
                    BufferedOutputStream(FileOutputStream(outputFile), bufferSize).use { bos ->
                        while (bis.read(buffer).also { bytesRead = it } != -1) {
                            if (isPaused) {
                                conn.disconnect()
                                return@launch
                            }

                            bos.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (fileLength > 0) {
                                val progressPercent = ((totalBytesRead * 100) / fileLength).toInt().coerceIn(0, 100)
                                val currentTime = System.currentTimeMillis()

                                if (progressPercent != lastProgressPercent && (currentTime - lastReportTime >= 250L || progressPercent == 100)) {
                                    lastProgressPercent = progressPercent
                                    lastReportTime = currentTime
                                    val progressFraction = totalBytesRead.toFloat() / fileLength.toFloat()
                                    withContext(Dispatchers.Main) {
                                        onProgress(progressFraction)
                                    }
                                }
                            }
                        }
                        bos.flush()
                    }
                }
                conn.disconnect()

                withContext(Dispatchers.Main) {
                    onDownloadComplete(outputFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: context.getString(R.string.download_failed))
                }
            }
        }
    }

    fun pauseDownload() {
        isPaused = true
        downloadJob?.cancel()
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }
}

// new download manager removed old download manager
