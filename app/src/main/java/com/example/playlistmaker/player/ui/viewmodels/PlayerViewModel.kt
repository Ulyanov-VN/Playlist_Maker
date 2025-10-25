package com.example.playlistmaker.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.interactor.*
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val formatTimeInteractor: FormatTimeInteractor,
    private val getCountryNameInteractor: GetCountryNameInteractor,
    private val getCoverArtworkInteractor: GetCoverArtworkInteractor,
    private val getReleaseYearInteractor: GetReleaseYearInteractor
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Stopped)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    var isFavorite = false
        private set
    var isInPlaylist = false
        private set

    private var currentTrack: Track? = null

    fun initialize(track: Track) {
        currentTrack = track

        playerInteractor.setOnPreparedListener {
            val duration = track.trackTimeMillis?.let { formatTimeInteractor.executeForTrack(it) } ?: "--:--"
            _state.value = PlayerState.Prepared(duration)
        }

        playerInteractor.setOnCompletionListener {
            _state.value = PlayerState.Stopped
        }

        playerInteractor.setOnErrorListener { errorMessage ->
            _state.value = PlayerState.Error(errorMessage)
        }

        playerInteractor.initialize(track)

        viewModelScope.launch {
            while (true) {
                if (playerInteractor.isPlaying()) {
                    _state.value = PlayerState.Playing(playerInteractor.getCurrentPosition())
                }
                kotlinx.coroutines.delay(500) // Update every 500ms
            }
        }
    }

    fun togglePlayPause() {
        if (!playerInteractor.isPrepared()) return

        if (playerInteractor.isPlaying()) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        playerInteractor.play()
        _state.value = PlayerState.Playing(playerInteractor.getCurrentPosition())
    }

    fun pausePlayback() {
        playerInteractor.pause()
        _state.value = PlayerState.Paused(playerInteractor.getCurrentPosition())
    }

    fun releasePlayer() {
        playerInteractor.release()
        _state.value = PlayerState.Stopped
    }

    fun toggleFavoriteState() {
        isFavorite = !isFavorite
    }

    fun togglePlaylistState() {
        isInPlaylist = !isInPlaylist
    }

    fun formatTime(millis: Long): String = formatTimeInteractor.execute(millis)
    fun getCountryName(countryCode: String?): String = getCountryNameInteractor.execute(countryCode)
    fun getCoverArtwork(artworkUrl100: String?): String? = getCoverArtworkInteractor.execute(artworkUrl100)
    fun getReleaseYear(releaseDate: String?): String? = getReleaseYearInteractor.execute(releaseDate)
}