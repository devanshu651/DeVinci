package com.example.devinci.service

import com.example.devinci.model.MusicResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApi {
    // Using Saavn API for full songs
    @GET("api/search/songs")
    suspend fun searchSongs(
        @Query("query") query: String
    ): MusicResponse
}
