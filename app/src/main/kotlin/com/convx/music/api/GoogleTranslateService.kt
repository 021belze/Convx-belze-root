/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

object GoogleTranslateService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun translate(
        text: String,
        targetLanguage: String = "id",
        sourceLanguage: String = "auto",
        maxRetries: Int = 2
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.failure(Exception("Input text is empty"))
        }

        val effectiveTargetLang = normalizeLanguageCode(targetLanguage)
        val lines = text.lines()
        val originalLineCount = lines.size

        var currentAttempt = 0
        var lastException: Exception? = null

        while (currentAttempt <= maxRetries) {
            try {
                val formBody = FormBody.Builder()
                    .add("q", text)
                    .build()

                val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLanguage&tl=$effectiveTargetLang&dt=t"

                val request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "*/*")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Google Translate HTTP ${response.code}: ${response.message}")
                    }

                    val responseBody = response.body?.string()
                        ?: throw Exception("Empty response body from Google Translate")

                    val rootArray = JSONArray(responseBody)
                    val sentencesArray = rootArray.optJSONArray(0)
                        ?: throw Exception("Invalid Google Translate response format")

                    val translatedBuilder = StringBuilder()
                    for (i in 0 until sentencesArray.length()) {
                        val sentence = sentencesArray.optJSONArray(i)
                        if (sentence != null && sentence.length() > 0) {
                            translatedBuilder.append(sentence.optString(0))
                        }
                    }

                    val fullTranslatedText = translatedBuilder.toString()
                    val translatedLines = fullTranslatedText.lines().toMutableList()

                    // Ensure returned line count matches or is safely padded
                    while (translatedLines.size < originalLineCount) {
                        translatedLines.add("")
                    }

                    return@withContext Result.success(translatedLines.take(originalLineCount))
                }
            } catch (e: Exception) {
                lastException = e
                currentAttempt++
                Timber.w(e, "Google Translate attempt $currentAttempt failed")
                if (currentAttempt <= maxRetries) {
                    kotlinx.coroutines.delay(500L * currentAttempt)
                }
            }
        }

        Result.failure(lastException ?: Exception("Google Translate failed after retries"))
    }

    private fun normalizeLanguageCode(lang: String): String {
        val trimmed = lang.trim().lowercase(Locale.ROOT)
        return when {
            trimmed.isBlank() || trimmed == "system" -> Locale.getDefault().language.ifBlank { "id" }
            trimmed in listOf("zh", "zh-cn", "zh-hans") -> "zh-CN"
            trimmed in listOf("zh-tw", "zh-hant") -> "zh-TW"
            trimmed.contains("-") -> trimmed.split("-").first()
            else -> trimmed
        }
    }
}
