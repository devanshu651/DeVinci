package com.example.devinci.ui

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.devinci.model.DisplayItem
import com.example.devinci.model.Song
import com.example.devinci.service.MusicService
import com.example.devinci.service.RetrofitClient
import com.example.devinci.utils.MusicScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var mediaController: MediaController? = null

    private val _localSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _onlineSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null
    
    val songs: StateFlow<List<DisplayItem>> = combine(_localSongs, _onlineSongs, _searchQuery) { local, online, query ->
        val filteredList = if (query.isBlank()) {
            local
        } else {
            local.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) } + online
        }
        
        if (filteredList.isEmpty()) {
            emptyList()
        } else {
            filteredList.map { DisplayItem.SongItem(it) } + DisplayItem.FooterItem
        }
    }.asStateFlow(viewModelScope, emptyList())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isShuffleModeEnabled = MutableStateFlow(false)
    val isShuffleModeEnabled: StateFlow<Boolean> = _isShuffleModeEnabled.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicService::class.java)
        )
        viewModelScope.launch {
            try {
                val controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
                mediaController = controllerFuture.await()
                setupControllerListener()
                
                val controller = mediaController
                if (controller != null && controller.mediaItemCount == 0 && _localSongs.value.isNotEmpty()) {
                    setupQueue(_localSongs.value)
                } else if (controller != null && controller.mediaItemCount > 0) {
                    syncCurrentSongWithController()
                }
            } catch (e: Exception) {
                Log.e("DeVinci", "Failed to initialize MediaController", e)
            }
        }
    }

    private fun setupControllerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSongFromMediaItem(mediaItem)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffleModeEnabled.value = shuffleModeEnabled
            }
        })
    }

    private fun syncCurrentSongWithController() {
        mediaController?.currentMediaItem?.let { updateCurrentSongFromMediaItem(it) }
        _isPlaying.value = mediaController?.isPlaying ?: false
        _isShuffleModeEnabled.value = mediaController?.shuffleModeEnabled ?: false
    }

    private fun updateCurrentSongFromMediaItem(mediaItem: MediaItem?) {
        val songUri = mediaItem?.mediaId
        val song = _localSongs.value.find { it.uri.toString() == songUri }
            ?: _onlineSongs.value.find { it.uri.toString() == songUri }
        _currentSong.value = song
    }

    fun loadSongs() {
        viewModelScope.launch {
            val scanner = MusicScanner()
            val loadedSongs = scanner.scanLocalAudioFiles(getApplication())
            _localSongs.value = loadedSongs
            
            mediaController?.let {
                if (it.mediaItemCount == 0) {
                    setupQueue(loadedSongs)
                }
            }
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _onlineSongs.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(600)
            try {
                val response = RetrofitClient.instance.searchSongs(query)
                val results = response.data?.results ?: emptyList()
                
                val newOnlineSongs = results.map { onlineSong ->
                    val streamUrl = onlineSong.downloadUrl?.lastOrNull()?.let { it.link ?: it.url } ?: ""
                    val artworkUrl = onlineSong.image?.lastOrNull()?.let { it.link ?: it.url } ?: ""
                    val artistNames = onlineSong.artists?.primary?.joinToString { it.name ?: "Unknown" } ?: "Unknown"

                    Song(
                        id = onlineSong.id?.hashCode()?.toLong() ?: 0L,
                        title = onlineSong.name ?: "Unknown",
                        artist = artistNames,
                        duration = (onlineSong.duration ?: 0) * 1000L,
                        uri = Uri.parse(streamUrl),
                        albumArtUri = Uri.parse(artworkUrl)
                    )
                }
                _onlineSongs.value = newOnlineSongs
            } catch (e: Exception) {
                Log.e("DeVinci", "Search failed", e)
            }
        }
    }

    private fun setupQueue(songs: List<Song>) {
        val controller = mediaController ?: return
        val mediaItems = songs.map { createMediaItem(it) }
        controller.setMediaItems(mediaItems)
        controller.prepare()
    }

    private fun createMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.uri.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(song.albumArtUri)
                    .build()
            )
            .build()
    }

    fun playSong(song: Song) {
        mediaController?.let { controller ->
            var indexInQueue = -1
            for (i in 0 until controller.mediaItemCount) {
                if (controller.getMediaItemAt(i).mediaId == song.uri.toString()) {
                    indexInQueue = i
                    break
                }
            }

            if (indexInQueue != -1) {
                controller.seekTo(indexInQueue, 0)
                controller.play()
            } else {
                val currentDisplayList = _localSongs.value.filter { 
                    it.title.contains(_searchQuery.value, ignoreCase = true) || 
                    it.artist.contains(_searchQuery.value, ignoreCase = true) 
                } + _onlineSongs.value

                val mediaItems = currentDisplayList.map { createMediaItem(it) }
                controller.setMediaItems(mediaItems)
                val newIndex = currentDisplayList.indexOf(song)
                if (newIndex != -1) {
                    controller.seekTo(newIndex, 0)
                }
                controller.prepare()
                controller.play()
            }
        }
    }

    fun playNext(song: Song) {
        mediaController?.let {
            val nextIndex = it.currentMediaItemIndex + 1
            it.addMediaItem(nextIndex, createMediaItem(song))
        }
    }

    fun addToQueue(song: Song) {
        mediaController?.addMediaItem(createMediaItem(song))
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun toggleShuffle() {
        mediaController?.let {
            val isEnabled = !it.shuffleModeEnabled
            it.shuffleModeEnabled = isEnabled
            _isShuffleModeEnabled.value = isEnabled
        }
    }
    
    fun seekTo(positionMs: Long) {
         mediaController?.seekTo(positionMs)
    }

    fun getDuration(): Long = mediaController?.duration ?: 0L
    fun getCurrentPosition(): Long = mediaController?.currentPosition ?: 0L

    override fun onCleared() {
        mediaController?.release()
        super.onCleared()
    }
}

private fun <T> kotlinx.coroutines.flow.Flow<T>.asStateFlow(
    scope: kotlinx.coroutines.CoroutineScope,
    initialValue: T
): StateFlow<T> {
    val state = MutableStateFlow(initialValue)
    scope.launch {
        this@asStateFlow.collect { state.value = it }
    }
    return state.asStateFlow()
}
