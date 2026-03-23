package com.example.playlistmaker.player.domain.repository

interface MediaRepository {
    fun initialize(previewUrl: String, callbacks: MediaCallbacks)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun isPlaying(): Boolean
    fun isPrepared(): Boolean
    fun seekTo(positionMs: Int)
}

interface MediaCallbacks {
    fun onPrepared()
    fun onCompletion()
    fun onError(errorMessage: String)
}