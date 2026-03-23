package com.example.playlistmaker.playlist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.model.Playlist
import kotlinx.coroutines.launch
import java.io.File

sealed class CreatePlaylistState {
    object Initial : CreatePlaylistState()
    object Creating : CreatePlaylistState()
    data class Success(val playlistId: Long, val playlistName: String) : CreatePlaylistState()
    data class Error(val message: String) : CreatePlaylistState()
}

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<CreatePlaylistState>(CreatePlaylistState.Initial)
    val state: LiveData<CreatePlaylistState> = _state

    private val _hasUnsavedChanges = MutableLiveData(false)
    val hasUnsavedChanges: LiveData<Boolean> = _hasUnsavedChanges

    private val _createButtonEnabled = MutableLiveData(false)
    val createButtonEnabled: LiveData<Boolean> = _createButtonEnabled

    private var coverImageFile: File? = null
    private var playlistName: String = ""
    private var playlistDescription: String = ""

    fun updateCoverImage(file: File?) {
        coverImageFile = file
        checkUnsavedChanges()
    }

    fun updateName(name: String) {
        playlistName = name.trim()
        _createButtonEnabled.value = playlistName.isNotBlank()
        checkUnsavedChanges()
    }

    fun updateDescription(description: String) {
        playlistDescription = description.trim()
        checkUnsavedChanges()
    }

    fun createPlaylist() {
        if (playlistName.isBlank()) return

        _state.value = CreatePlaylistState.Creating

        viewModelScope.launch {
            try {
                val playlist = Playlist(
                    name = playlistName,
                    description = if (playlistDescription.isNotBlank()) playlistDescription else null,
                    coverImagePath = coverImageFile?.absolutePath,
                    trackIds = emptyList(),
                    trackCount = 0
                )

                val playlistId = playlistInteractor.createPlaylist(playlist)

                _state.value = CreatePlaylistState.Success(
                    playlistId = playlistId,
                    playlistName = playlistName
                )

                // Сбрасываем состояние
                resetState()

            } catch (e: Exception) {
                _state.value = CreatePlaylistState.Error(
                    e.message ?: "Ошибка создания плейлиста"
                )
            }
        }
    }

    fun resetUnsavedChanges() {
        _hasUnsavedChanges.value = false
    }

    private fun checkUnsavedChanges() {
        val hasChanges = coverImageFile != null ||
                playlistName.isNotBlank() ||
                playlistDescription.isNotBlank()
        _hasUnsavedChanges.value = hasChanges
    }

    private fun resetState() {
        coverImageFile = null
        playlistName = ""
        playlistDescription = ""
        _hasUnsavedChanges.value = false
        _createButtonEnabled.value = false
    }
}