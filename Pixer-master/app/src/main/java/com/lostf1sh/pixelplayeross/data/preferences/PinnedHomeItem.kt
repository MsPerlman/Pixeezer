package com.lostf1sh.pixelplayeross.data.preferences

import kotlinx.serialization.Serializable

enum class PinnedItemType { FLOW, DISCOVERY, MIX, PLAYLIST, ARTIST, ALBUM, TRACK }

@Serializable
data class PinnedHomeItem(
    val type: PinnedItemType,
    val id: String,
    val label: String,
    val imageUrl: String? = null,
    val fallbackTrackArtUrls: List<String> = emptyList(),
    val addedAt: Long = System.currentTimeMillis()
)
