package com.example.playlistmaker.playlist.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailsViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val application: Application
) : ViewModel() {

    private val resources = application.resources

    private val _state = MutableLiveData<PlaylistDetailsUiState?>()
    val state: LiveData<PlaylistDetailsUiState?> = _state

    private val _playlistDeleted = MutableLiveData<Boolean>()
    val playlistDeleted: LiveData<Boolean> = _playlistDeleted

    fun load(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist: Playlist = playlistInteractor.getPlaylistById(playlistId)
                    ?: run {
                        _state.postValue(
                            PlaylistDetailsUiState(
                                name = "Плейлист не найден",
                                description = null,
                                coverPath = null,
                                totalMinutesText = "0 минут",
                                trackCountText = "0 треков",
                                tracks = emptyList()
                            )
                        )
                        return@launch
                    }

                val tracks: List<Track> = playlistInteractor.getTracksByIds(playlist.trackIds)

                var durationSum = 0L
                for (t in tracks) durationSum += (t.trackTimeMillis ?: 0L)
                val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(durationSum)

                val minutesText = resources.getQuantityString(R.plurals.minutes_count, minutes.toInt(), minutes.toInt())

                val trackCount = if (playlist.trackCount > 0) playlist.trackCount else playlist.trackIds.size

                val trackCountText = resources.getQuantityString(R.plurals.tracks_count, trackCount, trackCount)

                _state.postValue(
                    PlaylistDetailsUiState(
                        name = playlist.name,
                        description = playlist.description,
                        coverPath = playlist.coverImagePath,
                        totalMinutesText = minutesText,
                        trackCountText = trackCountText,
                        tracks = tracks
                    )
                )
            } catch (e: Exception) {

                _state.postValue(
                    PlaylistDetailsUiState(
                        name = "Ошибка",
                        description = e.message ?: "Не удалось загрузить плейлист",
                        coverPath = null,
                        totalMinutesText = "0 минут",
                        trackCountText = "0 треков",
                        tracks = emptyList()
                    )
                )
            }
        }
    }

    fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(playlistId, trackId)
            load(playlistId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(playlistId)
            _playlistDeleted.postValue(true)
        }
    }


    fun buildShareText(): String? {
        val st = _state.value ?: return null
        if (st.tracks.isEmpty()) return null

        val sb = StringBuilder()
        sb.append(st.name).append("\n")

        val desc = st.description?.trim().orEmpty()
        if (desc.isNotEmpty()) {
            sb.append(desc).append("\n")
        }


        sb.append("${st.tracks.size} треков").append("\n")

        for (i in st.tracks.indices) {
            val t = st.tracks[i]
            val artist = t.artistName?.trim().orEmpty().ifEmpty { "Unknown artist" }
            val title = t.trackName?.trim().orEmpty().ifEmpty { "Unknown track" }
            val time = formatTrackTime(t.trackTimeMillis ?: 0L)
            sb.append("${i + 1}. $artist - $title ($time)")
            if (i != st.tracks.lastIndex) sb.append("\n")
        }

        return sb.toString()
    }

    private fun formatTrackTime(millis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
    }

}
