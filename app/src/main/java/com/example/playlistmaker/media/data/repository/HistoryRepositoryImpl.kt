package com.example.playlistmaker.media.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.media.domain.repository.HistoryRepository
import com.example.playlistmaker.search.domain.entity.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepositoryImpl(
    private val prefs: SharedPreferences
) : HistoryRepository {

    private val gson = Gson()
    private val type = object : TypeToken<List<Track>>() {}.type

    override fun getHistory(): List<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return gson.fromJson(json, type)
    }

    override fun saveTrack(track: Track) {
        val list = getHistory().toMutableList()
        list.removeAll { it.trackId == track.trackId }
        list.add(0, track)
        if (list.size > MAX_SIZE) {
            list.subList(MAX_SIZE, list.size).clear()
        }
        prefs.edit()
            .putString(KEY_HISTORY, gson.toJson(list))
            .apply()
    }

    override fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }
}