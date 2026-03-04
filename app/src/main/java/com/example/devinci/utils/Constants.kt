package com.example.devinci.utils

import android.os.Environment
import java.io.File

object Constants {
    val MUSIC_FOLDER_PATH = File(Environment.getExternalStorageDirectory(), "Music").absolutePath
    
    // TODO: Replace these with your actual Spotify Client credentials from developer.spotify.com
    const val SPOTIFY_CLIENT_ID = "YOUR_CLIENT_ID"
    const val SPOTIFY_CLIENT_SECRET = "YOUR_CLIENT_SECRET"
}
