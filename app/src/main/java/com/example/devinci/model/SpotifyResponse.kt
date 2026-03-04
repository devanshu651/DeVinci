package com.example.devinci.model

import com.google.gson.annotations.SerializedName

data class SpotifyResponse(
    @SerializedName("tracks") val tracks: SpotifyTracks?
)

data class SpotifyTracks(
    @SerializedName("items") val items: List<SpotifyTrack>?
)

data class SpotifyTrack(
    val id: String,
    val name: String?,
    val artists: List<SpotifyArtist>?,
    val album: SpotifyAlbum?,
    @SerializedName("duration_ms") val durationMs: Long?,
    @SerializedName("preview_url") val previewUrl: String?
)

data class SpotifyArtist(val name: String?)

data class SpotifyAlbum(
    val name: String?,
    val images: List<SpotifyImage>?
)

data class SpotifyImage(val url: String?)

data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)
