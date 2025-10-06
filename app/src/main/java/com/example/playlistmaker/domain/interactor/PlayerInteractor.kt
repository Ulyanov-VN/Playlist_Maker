package com.example.playlistmaker.domain.interactor

import android.media.MediaPlayer
import android.util.Log
import com.example.playlistmaker.domain.entity.Track

interface PlayerInteractor {
    fun initialize(track: Track)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun isPlaying(): Boolean
    fun isPrepared(): Boolean
    fun setOnPreparedListener(listener: () -> Unit)
    fun setOnCompletionListener(listener: () -> Unit)
    fun setOnErrorListener(listener: (String) -> Unit)
}

class PlayerInteractorImpl : PlayerInteractor {

    private var mediaPlayer: MediaPlayer? = null
    private var onPreparedListener: (() -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    private var isPrepared = false
    private var currentTrack: Track? = null

    override fun initialize(track: Track) {
        currentTrack = track
        val previewUrl = track.previewUrl

        if (previewUrl.isNullOrEmpty()) {
            onErrorListener?.invoke("Preview URL is null or empty")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                setOnPreparedListener {
                    isPrepared = true
                    onPreparedListener?.invoke()
                }
                setOnCompletionListener {
                    onCompletionListener?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    val errorMsg = "MediaPlayer error: what=$what, extra=$extra"
                    Log.e("PlayerInteractor", errorMsg)
                    onErrorListener?.invoke(errorMsg)
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("PlayerInteractor", "Error initializing MediaPlayer", e)
            onErrorListener?.invoke("Error initializing player: ${e.message}")
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
        currentTrack = null
    }

    override fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    override fun getDuration(): Int = mediaPlayer?.duration ?: 0
    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    override fun isPrepared(): Boolean = isPrepared

    override fun setOnPreparedListener(listener: () -> Unit) {
        onPreparedListener = listener
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    override fun setOnErrorListener(listener: (String) -> Unit) {
        onErrorListener = listener
    }
}