package com.example.playlistmaker.player.ui.viewmodels

sealed class PlayerState {
    data object Stopped : PlayerState()
    data class Playing(val currentPosition: Int) : PlayerState()
    data class Paused(val currentPosition: Int) : PlayerState()
    data class Prepared(val trackDuration: String) : PlayerState()
    data class Error(val message: String) : PlayerState()
}