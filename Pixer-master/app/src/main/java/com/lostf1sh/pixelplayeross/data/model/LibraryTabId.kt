package com.lostf1sh.pixelplayeross.data.model

import androidx.compose.runtime.Immutable

@Immutable
enum class LibraryTabId(
    val storageKey: String,
    val title: String,
    val defaultSort: SortOption
) {
    ALBUMS("ALBUMS", "ALBUMS", SortOption.AlbumTitleAZ),
    ARTISTS("ARTIST", "ARTIST", SortOption.ArtistNameAZ),
    PLAYLISTS("PLAYLISTS", "PLAYLISTS", SortOption.PlaylistNameAZ),
    LIKED("LIKED", "LIKED", SortOption.LikedSongDateLiked),
    PODCASTS("PODCASTS", "PODCASTS", SortOption.PodcastTitleAZ);

    companion object {
        fun fromStorageKey(key: String): LibraryTabId =
            entries.firstOrNull { it.storageKey == key } ?: PLAYLISTS
    }
}

fun String.toLibraryTabIdOrNull(): LibraryTabId? =
    LibraryTabId.entries.firstOrNull { it.storageKey == this }