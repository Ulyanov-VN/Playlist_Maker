package com.example.playlistmaker.player.domain.service

import kotlinx.coroutines.flow.StateFlow

enum class ServicePlayerStatus {
    IDLE,
    PREPARED,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class ServicePlayerState(
    val status: ServicePlayerStatus = ServicePlayerStatus.IDLE,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val errorMessage: String? = null
)

interface AudioPlayerServiceController {
    fun play()
    fun pause()
    fun isPlaying(): Boolean
    fun enterForeground()
    fun exitForeground()
    fun stopServicePlayback()
    fun stateFlow(): StateFlow<ServicePlayerState>
}