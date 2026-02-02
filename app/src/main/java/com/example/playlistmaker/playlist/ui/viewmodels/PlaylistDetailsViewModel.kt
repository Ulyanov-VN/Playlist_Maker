package com.example.playlistmaker.playlist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

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

                val trackCount = if (playlist.trackCount > 0) playlist.trackCount else playlist.trackIds.size

                _state.postValue(
                    PlaylistDetailsUiState(
                        name = playlist.name,
                        description = playlist.description,
                        coverPath = playlist.coverImagePath,
                        totalMinutesText = "$minutes минут",
                        trackCountText = formatTrackCount(trackCount),
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

    // ✅ Шаг 4: удалить плейлист
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(playlistId)
            _playlistDeleted.postValue(true)
        }
    }

    // ✅ Шаг 4: текст для "Поделиться"
    // Возвращает null если треков нет
    fun buildShareText(): String? {
        val st = _state.value ?: return null
        if (st.tracks.isEmpty()) return null

        val sb = StringBuilder()
        sb.append(st.name).append("\n")

        val desc = st.description?.trim().orEmpty()
        if (desc.isNotEmpty()) {
            sb.append(desc).append("\n")
        }

        // по ТЗ: именно "[xx] треков"
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

    private fun formatTrackCount(count: Int): String {
        return when {
            count == 0 -> "0 треков"
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }
}
