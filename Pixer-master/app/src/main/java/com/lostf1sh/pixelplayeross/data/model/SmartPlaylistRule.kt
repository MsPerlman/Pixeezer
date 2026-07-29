package com.lostf1sh.pixelplayeross.data.model

import androidx.compose.runtime.Immutable

@Immutable
enum class SmartPlaylistRule(
    val storageKey: String
) {
    TOP_PLAYED(storageKey = "top_played"),
    RECENTLY_PLAYED(storageKey = "recently_played"),
    FORGOTTEN_FAVORITES(storageKey = "forgotten_favorites"),
    NEW_GEMS(storageKey = "new_gems");

    companion object {
        fun fromStorageKey(key: String?): SmartPlaylistRule? {
            if (key.isNullOrBlank()) return null
            return entries.firstOrNull { it.storageKey == key }
        }
    }
}

const val SMART_PLAYLIST_SOURCE_LEGACY = "SMART"
const val SMART_PLAYLIST_SOURCE_PREFIX = "$SMART_PLAYLIST_SOURCE_LEGACY:"

fun SmartPlaylistRule.toPlaylistSource(): String = "$SMART_PLAYLIST_SOURCE_PREFIX$storageKey"

fun SmartPlaylistRule.Companion.fromPlaylistSource(source: String): SmartPlaylistRule? {
    if (!source.startsWith(SMART_PLAYLIST_SOURCE_PREFIX)) return null
    return fromStorageKey(source.removePrefix(SMART_PLAYLIST_SOURCE_PREFIX))
}

fun isSmartPlaylistSource(source: String): Boolean =
    source == SMART_PLAYLIST_SOURCE_LEGACY || source.startsWith(SMART_PLAYLIST_SOURCE_PREFIX)

val Playlist.isSmartPlaylist: Boolean
    get() = isSmartPlaylistSource(source)
