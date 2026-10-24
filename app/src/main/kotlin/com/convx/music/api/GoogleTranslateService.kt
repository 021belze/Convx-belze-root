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

    private const val LINE_DELIMITER = "\n---\n"
    private val SPLIT_REGEX = Regex("""\r?\n?---\r?\n?""")

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
        val originalLines = text.lines()
        val originalLineCount = originalLines.size

        // Extract non-blank lines and record their original indices
        val indexedNonBlank = originalLines.mapIndexedNotNull { index, line ->
            if (line.isNotBlank()) index to line.trim() else null
        }

        if (indexedNonBlank.isEmpty()) {
            return@withContext Result.success(List(originalLineCount) { "" })
        }

        // Join non-blank lines with boundary delimiter to guarantee individual line translation without paragraph merging
        val payloadText = indexedNonBlank.joinToString(LINE_DELIMITER) { it.second }

        var currentAttempt = 0
        var lastException: Exception? = null

        while (currentAttempt <= maxRetries) {
            try {
                val formBody = FormBody.Builder()
                    .add("q", payloadText)
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
                    val segments = fullTranslatedText.split(SPLIT_REGEX)
                        .map { it.trim() }
                        .filter { it.isNotBlank() && it != "---" }

                    // Reconstruct the exact original lines preserving empty line positions
                    val resultLines = Array(originalLineCount) { "" }
                    for (i in indexedNonBlank.indices) {
                        val originalIndex = indexedNonBlank[i].first
                        val translatedSegment = segments.getOrNull(i) ?: ""
                        resultLines[originalIndex] = translatedSegment
                    }

                    return@withContext Result.success(resultLines.toList())
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
