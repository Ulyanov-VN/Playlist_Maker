package com.example.playlistmaker.player.domain.interactor

import com.example.playlistmaker.player.domain.repository.MediaRepository
import com.example.playlistmaker.search.domain.entity.Track

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
    fun seekTo(positionMs: Int)

}

class PlayerInteractorImpl(
    private val mediaRepository: MediaRepository
) : PlayerInteractor {

    private var onPreparedListener: (() -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var currentTrack: Track? = null

    override fun initialize(track: Track) {
        currentTrack = track
        val previewUrl = track.previewUrl.orEmpty()

        mediaRepository.initialize(previewUrl, object : com.example.playlistmaker.player.domain.repository.MediaCallbacks {
            override fun onPrepared() {
                onPreparedListener?.invoke()
            }

            override fun onCompletion() {
                onCompletionListener?.invoke()
            }

            override fun onError(errorMessage: String) {
                onErrorListener?.invoke(errorMessage)
            }
        })
    }

    override fun seekTo(positionMs: Int) {
        mediaRepository.seekTo(positionMs)
    }

    override fun play() {
        mediaRepository.play()
    }

    override fun pause() {
        mediaRepository.pause()
    }

    override fun release() {
        mediaRepository.release()
        currentTrack = null
    }

    override fun getCurrentPosition(): Int = mediaRepository.getCurrentPosition()
    override fun getDuration(): Int = mediaRepository.getDuration()
    override fun isPlaying(): Boolean = mediaRepository.isPlaying()
    override fun isPrepared(): Boolean = mediaRepository.isPrepared()

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