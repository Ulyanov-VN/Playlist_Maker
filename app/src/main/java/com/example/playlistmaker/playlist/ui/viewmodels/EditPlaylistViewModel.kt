package com.example.playlistmaker.playlist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.model.Playlist
import kotlinx.coroutines.launch

data class EditPlaylistUiState(
    val name: String,
    val description: String?,
    val coverPath: String?,
    val isSaveEnabled: Boolean
)

class EditPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<EditPlaylistUiState?>()
    val state: LiveData<EditPlaylistUiState?> = _state

    private val _saved = MutableLiveData<Boolean>()
    val saved: LiveData<Boolean> = _saved

    private var original: Playlist? = null

    private var name: String = ""
    private var description: String? = null
    private var coverPath: String? = null

    fun load(id: Long) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(id) ?: return@launch
            original = playlist

            name = playlist.name
            description = playlist.description
            coverPath = playlist.coverImagePath

            publishState()
        }
    }

    fun updateName(value: String) {
        name = value.trim()
        publishState()
    }

    fun updateDescription(value: String) {
        val trimmed = value.trim()
        description = if (trimmed.isBlank()) null else trimmed
        publishState()
    }

    fun updateCoverPath(path: String?) {
        coverPath = path
        publishState()
    }

    fun save() {
        if (name.isBlank()) return
        val base = original ?: return

        val updated = base.copy(
            name = name,
            description = description,
            coverImagePath = coverPath
        )

        viewModelScope.launch {
            playlistInteractor.updatePlaylist(updated)
            _saved.postValue(true)
        }
    }

    private fun publishState() {
        _state.postValue(
            EditPlaylistUiState(
                name = name,
                description = description,
                coverPath = coverPath,
                isSaveEnabled = name.isNotBlank()
            )
        )
    }
}
