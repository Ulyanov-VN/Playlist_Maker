package com.example.playlistmaker.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getReleaseYearInteractor: GetReleaseYearInteractor
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Stopped)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    var isFavorite = false
        private set
    var isInPlaylist = false
        private set

    private var currentTrack: Track? = null

    private var progressJob: Job? = null
    private var wasCompleted: Boolean = false

    companion object {
        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }

    fun initialize(track: Track) {
        currentTrack = track

        playerInteractor.setOnPreparedListener {
            wasCompleted = false
            val duration = track.trackTimeMillis?.let { formatTimeInteractor.executeForTrack(it) } ?: "--:--"
            _state.value = PlayerState.Prepared(duration)
        }

        playerInteractor.setOnCompletionListener {
            stopProgressUpdates()
            wasCompleted = true

            // Сброс позиции и UI таймера в 00:00
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

    fun togglePlayPause() {
        if (!playerInteractor.isPrepared()) return

        if (playerInteractor.isPlaying()) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        // Если трек закончился — стартуем строго с 00:00, без "вспышки" старого времени
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
