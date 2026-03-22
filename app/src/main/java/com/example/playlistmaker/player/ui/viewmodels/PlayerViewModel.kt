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
import com.example.playlistmaker.player.domain.service.AudioPlayerServiceController
import com.example.playlistmaker.player.domain.service.ServicePlayerStatus
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class AddToPlaylistStatus {
    data class Success(val playlistName: String) : AddToPlaylistStatus()
    data class AlreadyExists(val playlistName: String) : AddToPlaylistStatus()
    data class Error(val message: String) : AddToPlaylistStatus()
}

class PlayerViewModel(
    private val formatTimeInteractor: FormatTimeInteractor,
    private val getCountryNameInteractor: GetCountryNameInteractor,
    private val getCoverArtworkInteractor: GetCoverArtworkInteractor,
    private val getReleaseYearInteractor: GetReleaseYearInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val _state = MutableLiveData(PlayerState())
    val state: LiveData<PlayerState> = _state

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addToPlaylistStatus = MutableLiveData<AddToPlaylistStatus?>(null)
    val addToPlaylistStatus: LiveData<AddToPlaylistStatus?> = _addToPlaylistStatus

    private val _addedToPlaylistIds = MutableLiveData<Set<Long>>(emptySet())
    val addedToPlaylistIds: LiveData<Set<Long>> = _addedToPlaylistIds

    private var currentTrack: Track? = null
    private var playerController: AudioPlayerServiceController? = null

    private var playlistsJob: Job? = null
    private var serviceStateJob: Job? = null

    fun initialize(track: Track) {
        currentTrack = track

        _state.value = (_state.value ?: PlayerState()).copy(
            status = PlayerStatus.STOPPED,
            currentPosition = 0,
            trackDuration = formatTrackDuration(track),
            errorMessage = null
        )

        checkFavoriteStatus(track)
        loadPlaylists()
    }

    fun attachPlayerController(controller: AudioPlayerServiceController) {
        playerController = controller
        observeServiceState()
    }

    fun detachPlayerController() {
        serviceStateJob?.cancel()
        serviceStateJob = null
        playerController = null
    }

    private fun observeServiceState() {
        serviceStateJob?.cancel()

        val controller = playerController ?: return

        serviceStateJob = viewModelScope.launch {
            controller.stateFlow().collectLatest { serviceState ->
                val previousState = _state.value ?: PlayerState()

                val mappedStatus = when (serviceState.status) {
                    ServicePlayerStatus.IDLE -> PlayerStatus.STOPPED
                    ServicePlayerStatus.PREPARED -> PlayerStatus.PREPARED
                    ServicePlayerStatus.PLAYING -> PlayerStatus.PLAYING
                    ServicePlayerStatus.PAUSED -> PlayerStatus.PAUSED
                    ServicePlayerStatus.COMPLETED -> PlayerStatus.PAUSED
                    ServicePlayerStatus.ERROR -> PlayerStatus.ERROR
                }

                val mappedPosition = when (serviceState.status) {
                    ServicePlayerStatus.IDLE,
                    ServicePlayerStatus.COMPLETED -> 0
                    else -> serviceState.currentPosition
                }

                val mappedDuration = if (serviceState.duration > 0) {
                    formatTimeInteractor.executeForTrack(serviceState.duration.toLong())
                } else {
                    previousState.trackDuration.ifBlank { formatTrackDuration(currentTrack) }
                }

                _state.postValue(
                    previousState.copy(
                        status = mappedStatus,
                        currentPosition = mappedPosition,
                        trackDuration = mappedDuration,
                        errorMessage = serviceState.errorMessage
                    )
                )
            }
        }
    }

    private fun checkFavoriteStatus(track: Track) {
        viewModelScope.launch {
            try {
                val isFavorite = favoriteTracksInteractor.checkIsFavorite(track)
                _state.value = (_state.value ?: PlayerState()).copy(isFavorite = isFavorite)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking favorite status", e)
            }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val track = currentTrack ?: return@launch
            val currentState = _state.value ?: PlayerState()

            try {
                if (currentState.isFavorite) {
                    favoriteTracksInteractor.removeFromFavorites(track)
                    _state.value = currentState.copy(isFavorite = false)
                } else {
                    favoriteTracksInteractor.addToFavorites(track)
                    _state.value = currentState.copy(isFavorite = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorite status", e)
            }
        }
    }

    fun loadPlaylists() {
        playlistsJob?.cancel()
        playlistsJob = viewModelScope.launch {
            try {
                playlistInteractor.getAllPlaylists().collectLatest { playlists ->
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
                if (playlist.trackIds.contains(track.trackId)) {
                    _addToPlaylistStatus.value = AddToPlaylistStatus.AlreadyExists(playlist.name)
                    return@launch
                }

                val success = playlistInteractor.addTrackToPlaylist(playlist, track)

                if (success) {
                    val currentIds = _addedToPlaylistIds.value ?: emptySet()
                    _addedToPlaylistIds.value = currentIds + track.trackId
                    _addToPlaylistStatus.value = AddToPlaylistStatus.Success(playlist.name)
                    loadPlaylists()
                } else {
                    _addToPlaylistStatus.value = AddToPlaylistStatus.Error("Не удалось добавить трек")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding track to playlist", e)
                _addToPlaylistStatus.value =
                    AddToPlaylistStatus.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun clearAddToPlaylistStatus() {
        _addToPlaylistStatus.value = null
    }

    fun startPlayback() {
        playerController?.play()
    }

    fun pausePlayback() {
        playerController?.pause()
    }

    fun onUiBackgrounded(hasNotificationPermission: Boolean) {
        val currentState = _state.value ?: return
        if (currentState.status == PlayerStatus.PLAYING && hasNotificationPermission) {
            playerController?.enterForeground()
        }
    }

    fun onUiForegrounded() {
        playerController?.exitForeground()
    }

    fun onScreenClosing() {
        playerController?.stopServicePlayback()
    }

    fun formatTime(millis: Long): String {
        return formatTimeInteractor.execute(millis)
    }

    fun getCountryName(countryCode: String?): String {
        return getCountryNameInteractor.execute(countryCode)
    }

    fun getCoverArtwork(artworkUrl100: String?): String? {
        return getCoverArtworkInteractor.execute(artworkUrl100)
    }

    fun getReleaseYear(releaseDate: String?): String? {
        return getReleaseYearInteractor.execute(releaseDate)
    }

    private fun formatTrackDuration(track: Track?): String {
        return formatTimeInteractor.executeForTrack(track?.trackTimeMillis)
    }

    override fun onCleared() {
        super.onCleared()
        playlistsJob?.cancel()
        serviceStateJob?.cancel()
    }
}