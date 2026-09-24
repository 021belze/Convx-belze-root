package com.convx.music.vivimusic.updater.downloadmanager

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.convx.music.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class UpdateDownloadWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val apkUrl = inputData.getString("apk_url") ?: return@withContext Result.failure()
        val version = inputData.getString("version") ?: "unknown"
        val fileSize = inputData.getString("file_size") ?: ""

        DownloadNotificationManager.showDownloadStarting(version, fileSize)

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
                httpConn.setRequestProperty("User-Agent", "Convx-Updater/${version}")
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
                DownloadNotificationManager.showDownloadFailed(
                    version,
                    context.getString(R.string.server_error, conn.responseCode)
                )
                conn.disconnect()
                return@withContext Result.failure()
            }

            val fileLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conn.contentLengthLong.takeIf { it > 0 } ?: conn.contentLength.toLong()
            } else {
                conn.contentLength.toLong()
            }

            val downloadDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "vivi_updates"
            )
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val isZip = apkUrl.contains("nightly.link") || apkUrl.endsWith(".zip")
            val downloadFile = if (isZip) File(downloadDir, "vivi_temp.zip") else File(downloadDir, "vivi.apk")

            val bufferSize = 64 * 1024
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            var totalBytesRead: Long = 0
            var lastReportedProgress = -1
            var lastReportTime = 0L

            BufferedInputStream(conn.inputStream, bufferSize).use { bis ->
                BufferedOutputStream(FileOutputStream(downloadFile), bufferSize).use { bos ->
                    while (bis.read(buffer).also { bytesRead = it } != -1) {
                        if (isStopped) {
                            conn.disconnect()
                            if (downloadFile.exists()) {
                                downloadFile.delete()
                            }
                            return@withContext Result.retry()
                        }

                        bos.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (fileLength > 0) {
                            val progress = ((totalBytesRead * 100) / fileLength).toInt().coerceIn(0, 100)
                            val currentTime = System.currentTimeMillis()

                            // Throttle notification and WorkManager progress updates to avoid Binder IPC and DB thrashing
                            if (progress != lastReportedProgress && (currentTime - lastReportTime >= 300L || progress == 100)) {
                                lastReportedProgress = progress
                                lastReportTime = currentTime
                                DownloadNotificationManager.updateDownloadProgress(progress, version)
                                setProgress(workDataOf("progress" to (progress / 100f)))
                            }
                        }
                    }
                    bos.flush()
                }
            }
            conn.disconnect()

            val finalFile = if (isZip) {
                val targetApkFile = File(downloadDir, "vivi.apk")
                var extracted = false
                try {
                    BufferedInputStream(downloadFile.inputStream(), bufferSize).use { bis ->
                        ZipInputStream(bis).use { zis ->
                            var entry = zis.nextEntry
                            while (entry != null) {
                                if (!entry.isDirectory && entry.name.endsWith(".apk")) {
                                    BufferedOutputStream(FileOutputStream(targetApkFile), bufferSize).use { fos ->
                                        zis.copyTo(fos, bufferSize)
                                    }
                                    extracted = true
                                    break
                                }
                                entry = zis.nextEntry
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (downloadFile.exists()) downloadFile.delete()
                    DownloadNotificationManager.showDownloadFailed(
                        version,
                        e.message ?: "Failed to extract zip file"
                    )
                    return@withContext Result.failure()
                } finally {
                    if (downloadFile.exists()) {
                        downloadFile.delete()
                    }
                }
                if (!extracted) {
                    DownloadNotificationManager.showDownloadFailed(
                        version,
                        "Could not find APK in zip"
                    )
                    return@withContext Result.failure()
                }
                targetApkFile
            } else {
                downloadFile
            }

            DownloadNotificationManager.showDownloadComplete(version, finalFile.absolutePath)

            Result.success(workDataOf("file_path" to finalFile.absolutePath))
        } catch (e: Exception) {
            DownloadNotificationManager.showDownloadFailed(
                version,
                e.message ?: context.getString(R.string.download_failed)
            )
            Result.failure()
        }
    }
}
