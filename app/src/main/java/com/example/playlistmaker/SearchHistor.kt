package com.example.playlistmaker.data.history

import android.content.SharedPreferences
import com.example.playlistmaker.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }

    private val gson = Gson()
    private val type = object : TypeToken<List<Track>>() {}.type

    /** Вернёт последние сохранённые треки (новые наверху) */
    fun getHistory(): List<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return gson.fromJson(json, type)
    }

    /**
     * Сохранит трек:
     * — если он уже есть, уберёт старую запись;
     * — добавит в начало списка;
     * — урежет до MAX_SIZE.
     */
    fun saveTrack(track: Track) {
        val list = getHistory().toMutableList()
        list.removeAll { it.trackName == track.trackName && it.artistName == track.artistName }
        list.add(0, track)
        if (list.size > MAX_SIZE) {
            list.subList(MAX_SIZE, list.size).clear()
        }
        prefs.edit()
            .putString(KEY_HISTORY, gson.toJson(list))
            .apply()
    }

    /** Очистит всю историю */
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
