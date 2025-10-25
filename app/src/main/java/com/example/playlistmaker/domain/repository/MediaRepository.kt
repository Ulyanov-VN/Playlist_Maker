package com.example.playlistmaker.domain.repository

interface MediaRepository {
    fun initialize(previewUrl: String, callbacks: MediaCallbacks)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun isPlaying(): Boolean
    fun isPrepared(): Boolean
}

interface MediaCallbacks {
    fun onPrepared()
    fun onCompletion()
    fun onError(errorMessage: String)
}