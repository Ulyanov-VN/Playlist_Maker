package com.example.playlistmaker.player.data.repository

import android.media.MediaPlayer
import android.util.Log
import com.example.playlistmaker.player.domain.repository.MediaCallbacks
import com.example.playlistmaker.player.domain.repository.MediaRepository

class MediaRepositoryImpl : MediaRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var callbacks: MediaCallbacks? = null
    private var isPrepared = false

    override fun initialize(previewUrl: String, callbacks: MediaCallbacks) {
        this.callbacks = callbacks

        if (previewUrl.isEmpty()) {
            callbacks.onError("Preview URL is null or empty")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                setOnPreparedListener {
                    isPrepared = true
                    callbacks.onPrepared()
                }
                setOnCompletionListener {
                    try {
                        seekTo(0)
                    } catch (e: Exception) {
                        Log.e("MediaRepository", "Error seekTo(0) on completion", e)
                    }
                    callbacks.onCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    val errorMsg = "MediaPlayer error: what=$what, extra=$extra"
                    Log.e("MediaRepository", errorMsg)
                    callbacks.onError(errorMsg)
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Error initializing MediaPlayer", e)
            callbacks.onError("Error initializing player: ${e.message}")
        }
    }

    override fun play() {
        if (isPrepared) {
            mediaPlayer?.start()
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        callbacks = null
    }

    override fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
    }

    override fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    override fun getDuration(): Int = mediaPlayer?.duration ?: 0
    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    override fun isPrepared(): Boolean = isPrepared
}