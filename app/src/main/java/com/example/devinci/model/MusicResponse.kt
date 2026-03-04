package com.example.devinci.model

import com.google.gson.annotations.SerializedName

data class MusicResponse(
    @SerializedName("data") val data: MusicData?
)

data class MusicData(
    @SerializedName("results") val results: List<OnlineSong>?
)

data class OnlineSong(
    val id: String?,
    val name: String?,
    val album: OnlineAlbum?,
    val artists: ArtistInfo?,
    val duration: Int?,
    val image: List<OnlineImage>?,
    val downloadUrl: List<OnlineDownload>?
)

data class OnlineAlbum(val name: String?)

data class ArtistInfo(
    val primary: List<ArtistDetails>?
)

data class ArtistDetails(val name: String?)

data class OnlineImage(
    val link: String?,
    val url: String?,
    val quality: String?
)

data class OnlineDownload(
    val link: String?,
    val url: String?,
    val quality: String?
)
