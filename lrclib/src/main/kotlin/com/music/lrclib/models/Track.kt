package com.music.lrclib.models

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class Track(
    val id: Int,
    val trackName: String,
    val artistName: String,
    val duration: Double,
    val plainLyrics: String?,
    val syncedLyrics: String?,
)

internal fun List<Track>.bestMatchingFor(duration: Int): Track? {
    if (isEmpty()) return null

    if (duration == -1) {
        return firstOrNull { it.syncedLyrics != null } ?: firstOrNull()
    }

    return minByOrNull { abs(it.duration.toInt() - duration) }
        ?.takeIf { abs(it.duration.toInt() - duration) <= 2 }
}


internal fun List<Track>.bestMatchingFor(
    duration: Int,
    trackName: String? = null,
    artistName: String? = null
): Track? {
    if (isEmpty()) return null

    if (trackName != null && artistName != null) {
        return findBestMatch(trackName, artistName, duration)
    }

    if (duration == -1) {
        return firstOrNull { it.syncedLyrics != null } ?: firstOrNull()
    }

    return null
}

/**
 * Fast sanity check for synced lyrics against target duration.
 * Validates that timestamps fit within song bounds without heavy parsing.
 */
fun isValidLrcForDuration(syncedLyrics: String?, duration: Int): Boolean {
    if (syncedLyrics.isNullOrBlank() || duration <= 0) return true

    var firstTime: Double? = null
    var lastTime: Double? = null

    for (line in syncedLyrics.lineSequence()) {
        if (!line.startsWith("[")) continue
        val parsed = parseLrcTimestampSeconds(line) ?: continue
        if (firstTime == null) {
            firstTime = parsed
        }
        lastTime = parsed
    }

    if (firstTime == null || lastTime == null) return true

    // 1. Last timestamp cannot exceed song duration by more than 8 seconds
    // (Prevents extended club mix 290s lyrics matching a 165s radio edit)
    if (lastTime > (duration + 8.0)) {
        return false
    }

    // 2. Allow songs with long instrumental intros (up to 75% of duration)
    if (firstTime > (duration * 0.75)) {
        return false
    }

    return true
}

private fun parseLrcTimestampSeconds(line: String): Double? {
    val closeBracket = line.indexOf(']')
    if (closeBracket <= 1 || !line.startsWith("[")) return null
    val colon = line.indexOf(':')
    if (colon <= 1 || colon >= closeBracket) return null

    val minStr = line.substring(1, colon)
    val secStr = line.substring(colon + 1, closeBracket)

    val mins = minStr.toIntOrNull() ?: return null
    val secs = secStr.toDoubleOrNull() ?: return null

    return mins * 60.0 + secs
}

/**
 * Simple timing fingerprint from the first 2-3 lines of synced lyrics.
 * Groups independent uploads that agree on the same vocal start timing.
 */
fun timingFingerprint(syncedLyrics: String?): String {
    if (syncedLyrics.isNullOrBlank()) return ""
    val parts = mutableListOf<String>()
    for (line in syncedLyrics.lineSequence()) {
        val trimmed = line.trim()
        if (!trimmed.startsWith("[")) continue
        val parsedTime = parseLrcTimestampSeconds(trimmed) ?: continue
        val closeBracket = trimmed.indexOf(']')
        val rawText = if (closeBracket != -1 && closeBracket < trimmed.length - 1) {
            trimmed.substring(closeBracket + 1).trim()
        } else ""
        if (rawText.isBlank()) continue
        
        // 1.5s time bucket to tolerate slight sub-second differences between tools (e.g. 14.36s vs 14.68s)
        val bucket = (parsedTime / 1.5).toInt()
        val cleanText = rawText.filter { it.isLetterOrDigit() }.lowercase().take(10)
        parts.add("$bucket:$cleanText")
        if (parts.size >= 3) break
    }
    return parts.joinToString("|")
}

private fun List<Track>.findBestMatch(trackName: String, artistName: String, duration: Int = -1): Track? {
    val normalizedTrackName = trackName.trim().lowercase()
    val normalizedArtistName = artistName.trim().lowercase()
    
    // Open candidate pool: include all candidates with reasonable duration offset (<= 12s)
    // rather than hard cutoff tiers that discard high-quality synced lyrics.
    val candidates = if (duration > 0) {
        val closeTracks = filter { 
            abs(it.duration.toInt() - duration) <= 12 && 
            isValidLrcForDuration(it.syncedLyrics, duration)
        }
        if (closeTracks.isNotEmpty()) closeTracks else filter { isValidLrcForDuration(it.syncedLyrics, duration) }
    } else {
        this
    }.ifEmpty { this }

    // Build timing consensus clusters across candidates that have valid synced lyrics
    val clusters = candidates
        .filter { it.syncedLyrics != null && isValidLrcForDuration(it.syncedLyrics, duration) }
        .groupBy { timingFingerprint(it.syncedLyrics) }

    return candidates.maxByOrNull { track ->
        var score = 0.0

        val trackNameSimilarity = calculateSimilarity(
            normalizedTrackName, 
            track.trackName.trim().lowercase()
        )

        val artistNameSimilarity = calculateSimilarity(
            normalizedArtistName, 
            track.artistName.trim().lowercase()
        )
        
        score = (trackNameSimilarity * 2.0 + artistNameSimilarity) / 3.0

        // 1. PRIMARY PRIORITY: Valid synced karaoke lyrics heavily beat plain text
        if (track.syncedLyrics != null) {
            if (isValidLrcForDuration(track.syncedLyrics, duration)) {
                // High bonus (+3.5) so synced lyrics are never beaten by plain text due to slight duration variation
                score += 3.5

                // Richness bonus: reward complete lyrics with multiple timestamped lines over sparse placeholders
                val lineCount = track.syncedLyrics.lineSequence().count { it.startsWith("[") }
                if (lineCount >= 10) score += 0.5
                if (lineCount >= 20) score += 0.5

                // Community consensus bonus: reward tracks that belong to a consensus cluster
                val fp = timingFingerprint(track.syncedLyrics)
                val clusterSize = clusters[fp]?.size ?: 1
                if (clusterSize > 1) {
                    score += minOf((clusterSize - 1) * 0.3, 0.9)
                }
            } else {
                score -= 2.0 // Heavy penalty for invalid/out-of-bounds lyrics
            }
        } else if (track.plainLyrics != null) {
            score += 0.3 // Plain text lyrics serve as fallback only
        }

        // 2. Smooth duration bonus scaling
        if (duration > 0) {
            val diff = abs(track.duration.toInt() - duration)
            val durationBonus = when {
                diff <= 1 -> 1.2   // Exact audio master match
                diff <= 3 -> 0.9   // Minor silence pad difference
                diff <= 5 -> 0.6
                diff <= 8 -> 0.3
                diff <= 12 -> 0.0
                else -> -(diff * 0.1) // Penalty for distant durations
            }
            score += durationBonus
        }
        
        score
    }?.takeIf { track ->
        val trackNameSimilarity = calculateSimilarity(
            normalizedTrackName, 
            track.trackName.trim().lowercase()
        )
        val artistNameSimilarity = calculateSimilarity(
            normalizedArtistName, 
            track.artistName.trim().lowercase()
        )

        val isNameMatch = trackNameSimilarity >= 0.45 ||
            normalizedTrackName.contains(track.trackName.trim().lowercase()) ||
            track.trackName.trim().lowercase().contains(normalizedTrackName)
        val isArtistMatch = artistNameSimilarity >= 0.35 || normalizedArtistName.isBlank() ||
            normalizedArtistName.contains(track.artistName.trim().lowercase()) ||
            track.artistName.trim().lowercase().contains(normalizedArtistName)

        isNameMatch && isArtistMatch
    }
}

private fun calculateSimilarity(str1: String, str2: String): Double {
    if (str1 == str2) return 1.0
    if (str1.isEmpty() || str2.isEmpty()) return 0.0

    val containsScore = when {
        str1.contains(str2) || str2.contains(str1) -> 0.8
        else -> 0.0
    }

    val maxLength = maxOf(str1.length, str2.length)
    val distance = levenshteinDistance(str1, str2)
    val distanceScore = 1.0 - (distance.toDouble() / maxLength)
    
    return maxOf(containsScore, distanceScore)
}

private fun levenshteinDistance(str1: String, str2: String): Int {
    val len1 = str1.length
    val len2 = str2.length
    val matrix = Array(len1 + 1) { IntArray(len2 + 1) }
    
    for (i in 0..len1) matrix[i][0] = i
    for (j in 0..len2) matrix[0][j] = j
    
    for (i in 1..len1) {
        for (j in 1..len2) {
            val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
            matrix[i][j] = minOf(
                matrix[i - 1][j] + 1,      // deletion
                matrix[i][j - 1] + 1,      // insertion
                matrix[i - 1][j - 1] + cost // substitution
            )
        }
    }
    
    return matrix[len1][len2]
}
