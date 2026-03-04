package com.example.devinci.model

import android.net.Uri

sealed class DisplayItem {
    data class SongItem(val song: Song) : DisplayItem()
    object FooterItem : DisplayItem()
}

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri? = null
)
