package com.example.playlistmaker.domain.interactor

import java.util.Locale

class FormatTimeInteractor {
    fun execute(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun executeForTrack(millis: Long?): String {
        return millis?.let { execute(it) } ?: "--:--"
    }
}