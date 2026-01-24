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
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var currentTrack: Track? = null

    private var progressJob: Job? = null
    private var wasCompleted: Boolean = false

    companion object {
        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }

    fun initialize(track: Track) {
        currentTrack = track
        wasCompleted = false

        // загрузим избранное в общий state
        checkFavoriteStatus(track)

        playerInteractor.setOnPreparedListener {
            wasCompleted = false

            val duration = track.trackTimeMillis
                ?.let { formatTimeInteractor.executeForTrack(it) }
                ?: "--:--"

            _state.value = _state.value.copy(
                status = PlayerStatus.PREPARED,
                trackDuration = duration,
                currentPosition = 0,
                errorMessage = null
            )
        }

        playerInteractor.setOnCompletionListener {
            stopProgressUpdates()
            wasCompleted = true

            // Требование: после завершения прогресс = 00:00
            playerInteractor.seekTo(0)

            _state.value = _state.value.copy(
                status = PlayerStatus.PAUSED,   // UI показывает "на паузе" на 00:00
                currentPosition = 0,
                errorMessage = null
            )
        }

        playerInteractor.setOnErrorListener { errorMessage ->
            stopProgressUpdates()
            wasCompleted = false

            _state.value = _state.value.copy(
                status = PlayerStatus.ERROR,
                errorMessage = errorMessage
            )
        }

        playerInteractor.initialize(track)
    }

    private fun checkFavoriteStatus(track: Track) {
        viewModelScope.launch {
            val fav = favoriteTracksInteractor.checkIsFavorite(track)
            _state.value = _state.value.copy(isFavorite = fav)
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val track = currentTrack ?: return@launch

            if (_state.value.isFavorite) {
                favoriteTracksInteractor.removeFromFavorites(track)
                _state.value = _state.value.copy(isFavorite = false)
            } else {
                favoriteTracksInteractor.addToFavorites(track)
                _state.value = _state.value.copy(isFavorite = true)
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
        // если трек завершился — стартуем строго с 00:00 без вспышек старого времени
        if (wasCompleted) {
            playerInteractor.seekTo(0)
            wasCompleted = false

            _state.value = _state.value.copy(
                status = PlayerStatus.PLAYING,
                currentPosition = 0,
                errorMessage = null
            )
        } else {
            _state.value = _state.value.copy(
                status = PlayerStatus.PLAYING,
                currentPosition = playerInteractor.getCurrentPosition(),
                errorMessage = null
            )
        }

        playerInteractor.play()
        startProgressUpdates()
    }

    fun pausePlayback() {
        playerInteractor.pause()
        stopProgressUpdates()

        _state.value = _state.value.copy(
            status = PlayerStatus.PAUSED,
            currentPosition = playerInteractor.getCurrentPosition(),
            errorMessage = null
        )
    }

    fun releasePlayer() {
        stopProgressUpdates()
        playerInteractor.release()
        wasCompleted = false

        _state.value = _state.value.copy(
            status = PlayerStatus.STOPPED,
            currentPosition = 0,
            errorMessage = null
        )
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && playerInteractor.isPlaying()) {
                _state.value = _state.value.copy(
                    status = PlayerStatus.PLAYING,
                    currentPosition = playerInteractor.getCurrentPosition()
                )
                delay(PROGRESS_UPDATE_DELAY_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
    }

    fun formatTime(millis: Long): String = formatTimeInteractor.execute(millis)
    fun getCountryName(countryCode: String?): String = getCountryNameInteractor.execute(countryCode)
    fun getCoverArtwork(artworkUrl100: String?): String? = getCoverArtworkInteractor.execute(artworkUrl100)
    fun getReleaseYear(releaseDate: String?): String? = getReleaseYearInteractor.execute(releaseDate)
}