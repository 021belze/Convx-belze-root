/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.lyrics

import com.convx.music.constants.PreferredLyricsProvider

/**
 * Central registry for all lyrics providers.
 * Maps provider names (used for persistence) to provider objects,
 * and handles serialization/deserialization of the custom priority order.
 */
object LyricsProviderRegistry {
    private val providerMap = mapOf(
        "LrcLib"           to LrcLibLyricsProvider,
        "YouTubeMusic"     to YouTubeLyricsProvider,
        "YouTube Music"    to YouTubeLyricsProvider,
        "YouTubeSubtitle"  to YouTubeSubtitleLyricsProvider,
        "YouTube Subtitle" to YouTubeSubtitleLyricsProvider,
    )

    val providerNames = listOf("LrcLib", "YouTubeMusic", "YouTubeSubtitle")

    fun getProviderByName(name: String): LyricsProvider? = providerMap[name]

    fun deserializeProviderOrder(orderString: String): List<String> {
        if (orderString.isBlank()) return getDefaultProviderOrder()
        val filtered = orderString.split(",").map { it.trim() }.filter { it in providerNames || it in providerMap.keys }
        return if (filtered.isEmpty()) getDefaultProviderOrder() else filtered
    }

    fun serializeProviderOrder(providers: List<String>): String =
        providers.filter { it in providerNames || it in providerMap.keys }.joinToString(",")

    fun getDefaultProviderOrder(): List<String> = listOf(
        "LrcLib",
        "YouTubeMusic",
        "YouTubeSubtitle",
    )

    fun getOrderedProviders(orderString: String): List<LyricsProvider> =
        deserializeProviderOrder(orderString).mapNotNull { getProviderByName(it) }

    /** Maps a [PreferredLyricsProvider] enum value to its registry name, used for migration. */
    fun getProviderNameForEnum(enum: PreferredLyricsProvider): String = when (enum) {
        PreferredLyricsProvider.LRCLIB        -> "LrcLib"
        else                                  -> "LrcLib"
    }

    /** Returns the human-readable display name for a registry provider key. */
    fun getDisplayName(name: String): String = when (name) {
        "LrcLib"                              -> "LrcLib (Karaoke)"
        "YouTubeMusic", "YouTube Music"       -> "YouTube Music (Official)"
        "YouTubeSubtitle", "YouTube Subtitle" -> "YouTube Subtitle (CC)"
        else                                  -> name
    }
}
