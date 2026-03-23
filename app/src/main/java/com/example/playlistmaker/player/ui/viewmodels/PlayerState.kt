package com.example.playlistmaker.player.ui.viewmodels

enum class PlayerStatus {
    STOPPED,
    PREPARED,
    PLAYING,
    PAUSED,
    ERROR
}

data class PlayerState(
    val status: PlayerStatus = PlayerStatus.STOPPED,
    val currentPosition: Int = 0,         // текущее время трека (мс)
    val trackDuration: String = "--:--",   // длительность трека строкой
    val errorMessage: String? = null,      // текст ошибки
    val isFavorite: Boolean = false        // признак избранного
)