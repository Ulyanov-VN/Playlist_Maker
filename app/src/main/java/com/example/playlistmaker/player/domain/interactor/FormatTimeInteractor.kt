package com.example.playlistmaker.player.domain.interactor

interface FormatTimeInteractor {
    fun execute(millis: Long): String
    fun executeForTrack(millis: Long?): String
}

class FormatTimeInteractorImpl : FormatTimeInteractor {
    override fun execute(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun executeForTrack(millis: Long?): String {
        return millis?.let { execute(it) } ?: "--:--"
    }
}