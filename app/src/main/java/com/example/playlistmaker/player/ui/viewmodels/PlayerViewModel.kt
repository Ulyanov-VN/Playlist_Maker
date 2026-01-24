package com.example.playlistmaker.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.favorites.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.player.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.player.domain.interactor.GetCountryNameInteractor
import com.example.playlistmaker.player.domain.interactor.GetCoverArtworkInteractor
import com.example.playlistmaker.player.domain.interactor.GetReleaseYearInteractor
import com.example.playlistmaker.player.domain.interactor.PlayerInteractor
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val formatTimeInteractor: FormatTimeInteractor,
    private val getCountryNameInteractor: GetCountryNameInteractor,
    private val getCoverArtworkInteractor: GetCoverArtworkInteractor,
    private val getReleaseYearInteractor: GetReleaseYearInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor  // ДОБАВЛЯЕМ
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Stopped)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var currentTrack: Track? = null

    private var progressJob: Job? = null
    private var wasCompleted: Boolean = false

    companion object {
        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }

    fun initialize(track: Track) {
        currentTrack = track
        checkFavoriteStatus(track)

        playerInteractor.setOnPreparedListener {
            wasCompleted = false
            val duration = track.trackTimeMillis?.let { formatTimeInteractor.executeForTrack(it) } ?: "--:--"
            _state.value = PlayerState.Prepared(duration)
        }

        playerInteractor.setOnCompletionListener {
            stopProgressUpdates()
            wasCompleted = true
            playerInteractor.seekTo(0)
            _state.value = PlayerState.Paused(0)
        }

        playerInteractor.setOnErrorListener { errorMessage ->
            stopProgressUpdates()
            wasCompleted = false
            _state.value = PlayerState.Error(errorMessage)
        }

        playerInteractor.initialize(track)
    }

    private fun checkFavoriteStatus(track: Track) {
        viewModelScope.launch {
            val isFavoriteTrack = favoriteTracksInteractor.checkIsFavorite(track)
            _isFavorite.value = isFavoriteTrack
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                if (_isFavorite.value) {
                    favoriteTracksInteractor.removeFromFavorites(track)
                } else {
                    favoriteTracksInteractor.addToFavorites(track)
                }
                _isFavorite.value = !_isFavorite.value
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
        if (wasCompleted) {
            playerInteractor.seekTo(0)
            _state.value = PlayerState.Playing(0)
            wasCompleted = false
        } else {
            _state.value = PlayerState.Playing(playerInteractor.getCurrentPosition())
        }

        playerInteractor.play()
        startProgressUpdates()
    }

    fun pausePlayback() {
        playerInteractor.pause()
        stopProgressUpdates()
        _state.value = PlayerState.Paused(playerInteractor.getCurrentPosition())
    }

    fun releasePlayer() {
        stopProgressUpdates()
        playerInteractor.release()
        wasCompleted = false
        _state.value = PlayerState.Stopped
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && playerInteractor.isPlaying()) {
                _state.value = PlayerState.Playing(playerInteractor.getCurrentPosition())
                delay(PROGRESS_UPDATE_DELAY_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun formatTime(millis: Long): String = formatTimeInteractor.execute(millis)
    fun getCountryName(countryCode: String?): String = getCountryNameInteractor.execute(countryCode)
    fun getCoverArtwork(artworkUrl100: String?): String? = getCoverArtworkInteractor.execute(artworkUrl100)
    fun getReleaseYear(releaseDate: String?): String? = getReleaseYearInteractor.execute(releaseDate)
}