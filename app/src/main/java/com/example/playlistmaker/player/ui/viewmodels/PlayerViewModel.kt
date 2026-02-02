package com.example.playlistmaker.player.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.favorites.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.player.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.player.domain.interactor.GetCountryNameInteractor
import com.example.playlistmaker.player.domain.interactor.GetCoverArtworkInteractor
import com.example.playlistmaker.player.domain.interactor.GetReleaseYearInteractor
import com.example.playlistmaker.player.domain.interactor.PlayerInteractor
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val formatTimeInteractor: FormatTimeInteractor,
    private val getCountryNameInteractor: GetCountryNameInteractor,
    private val getCoverArtworkInteractor: GetCoverArtworkInteractor,
    private val getReleaseYearInteractor: GetReleaseYearInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData(PlayerState())
    val state: LiveData<PlayerState> = _state

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addToPlaylistStatus = MutableLiveData<AddToPlaylistStatus?>(null)
    val addToPlaylistStatus: LiveData<AddToPlaylistStatus?> = _addToPlaylistStatus

    // Новое состояние для отслеживания добавленных треков в текущей сессии
    private val _addedToPlaylistIds = MutableLiveData<Set<Long>>(emptySet())
    val addedToPlaylistIds: LiveData<Set<Long>> = _addedToPlaylistIds

    private var currentTrack: Track? = null
    private var progressJob: Job? = null
    private var wasCompleted: Boolean = false

    companion object {
        private const val PROGRESS_UPDATE_DELAY_MS = 300L
        private const val TAG = "PlayerViewModel"
    }

    fun initialize(track: Track) {
        currentTrack = track
        wasCompleted = false

        checkFavoriteStatus(track)
        loadPlaylists()

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
            playerInteractor.seekTo(0)

            _state.value = _state.value.copy(
                status = PlayerStatus.PAUSED,
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
            try {
                val fav = favoriteTracksInteractor.checkIsFavorite(track)
                _state.value = _state.value.copy(isFavorite = fav)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking favorite status", e)
            }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val track = currentTrack ?: return@launch
            try {
                if (_state.value.isFavorite) {
                    favoriteTracksInteractor.removeFromFavorites(track)
                    _state.value = _state.value.copy(isFavorite = false)
                } else {
                    favoriteTracksInteractor.addToFavorites(track)
                    _state.value = _state.value.copy(isFavorite = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorite", e)
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                playlistInteractor.getAllPlaylists().collect { playlists ->
                    _playlists.value = playlists
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading playlists", e)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val track = currentTrack ?: return@launch

            try {
                // Проверяем, есть ли трек уже в плейлисте
                if (playlist.trackIds.contains(track.trackId)) {
                    _addToPlaylistStatus.value = AddToPlaylistStatus.AlreadyExists(playlist.name)
                    return@launch
                }

                // Добавляем трек в плейлист
                val success = playlistInteractor.addTrackToPlaylist(playlist, track)

                if (success) {
                    // Добавляем ID трека в множество добавленных
                    val currentIds = _addedToPlaylistIds.value ?: emptySet()
                    _addedToPlaylistIds.value = currentIds + track.trackId

                    _addToPlaylistStatus.value = AddToPlaylistStatus.Success(playlist.name)
                    // Обновляем список плейлистов после добавления
                    loadPlaylists()
                } else {
                    _addToPlaylistStatus.value = AddToPlaylistStatus.Error("Ошибка добавления")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding track to playlist", e)
                _addToPlaylistStatus.value = AddToPlaylistStatus.Error("Ошибка: ${e.message}")
            }
        }
    }

    // Проверяем, добавлен ли текущий трек в какой-либо плейлист
    fun isTrackAddedToPlaylist(): Boolean {
        val trackId = currentTrack?.trackId ?: return false
        val addedIds = _addedToPlaylistIds.value ?: emptySet()
        return addedIds.contains(trackId)
    }

    fun clearAddToPlaylistStatus() {
        _addToPlaylistStatus.value = null
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
            try {
                while (playerInteractor.isPlaying()) {
                    _state.value = _state.value.copy(
                        status = PlayerStatus.PLAYING,
                        currentPosition = playerInteractor.getCurrentPosition()
                    )
                    delay(PROGRESS_UPDATE_DELAY_MS)
                }
            } catch (e: Exception) {
                // Корутина была отменена
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

sealed class AddToPlaylistStatus {
    data class Success(val playlistName: String) : AddToPlaylistStatus()
    data class AlreadyExists(val playlistName: String) : AddToPlaylistStatus()
    data class Error(val message: String) : AddToPlaylistStatus()
}