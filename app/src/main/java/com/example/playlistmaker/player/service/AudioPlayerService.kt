package com.example.playlistmaker.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.main.ui.MainActivity
import com.example.playlistmaker.player.domain.service.AudioPlayerServiceController
import com.example.playlistmaker.player.domain.service.ServicePlayerState
import com.example.playlistmaker.player.domain.service.ServicePlayerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioPlayerService : Service(), AudioPlayerServiceController {

    companion object {
        private const val TAG = "AudioPlayerService"

        const val EXTRA_PREVIEW_URL = "extra_preview_url"
        const val EXTRA_TRACK_NAME = "extra_track_name"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"

        private const val NOTIFICATION_CHANNEL_ID = "playlist_maker_player_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Playlist Maker Player"
        private const val NOTIFICATION_ID = 1001
        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }

    inner class PlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    private val binder = PlayerBinder()

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(ServicePlayerState())

    private var currentTrackKey: String? = null
    private var previewUrl: String = ""
    private var trackName: String = ""
    private var artistName: String = ""

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val newPreviewUrl = intent?.getStringExtra(EXTRA_PREVIEW_URL).orEmpty()
        val newTrackName = intent?.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        val newArtistName = intent?.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()

        if (newPreviewUrl.isNotBlank()) {
            val newTrackKey = "$newPreviewUrl|$newTrackName|$newArtistName"

            val shouldPrepare =
                currentTrackKey != newTrackKey ||
                        mediaPlayer == null ||
                        _state.value.status == ServicePlayerStatus.IDLE ||
                        _state.value.status == ServicePlayerStatus.ERROR

            if (shouldPrepare) {
                previewUrl = newPreviewUrl
                trackName = newTrackName
                artistName = newArtistName
                currentTrackKey = newTrackKey
                preparePlayer(previewUrl)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        exitForeground()
        releasePlayer()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun preparePlayer(url: String) {
        Log.d(TAG, "preparePlayer url=$url")
        releasePlayer()

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)

                setOnPreparedListener { player ->
                    Log.d(TAG, "MediaPlayer prepared, duration=${player.duration}")
                    _state.value = ServicePlayerState(
                        status = ServicePlayerStatus.PREPARED,
                        currentPosition = 0,
                        duration = player.duration,
                        errorMessage = null
                    )
                }

                setOnCompletionListener {
                    Log.d(TAG, "MediaPlayer completed")
                    stopProgressUpdates()
                    exitForeground()

                    try {
                        seekTo(0)
                    } catch (e: Exception) {
                        Log.e(TAG, "seekTo(0) on completion error", e)
                    }

                    _state.value = _state.value.copy(
                        status = ServicePlayerStatus.COMPLETED,
                        currentPosition = 0,
                        errorMessage = null
                    )

                    stopSelf()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    stopProgressUpdates()
                    exitForeground()

                    _state.value = _state.value.copy(
                        status = ServicePlayerStatus.ERROR,
                        errorMessage = "MediaPlayer error: what=$what, extra=$extra"
                    )
                    true
                }

                prepareAsync()
                Log.d(TAG, "prepareAsync called")
            } catch (e: Exception) {
                Log.e(TAG, "preparePlayer exception", e)
                _state.value = ServicePlayerState(
                    status = ServicePlayerStatus.ERROR,
                    errorMessage = e.message
                )
            }
        }
    }

    override fun play() {
        val player = mediaPlayer
        Log.d(TAG, "play called, state=${_state.value.status}, playerNull=${player == null}")

        if (player == null) return

        val canPlay = _state.value.status == ServicePlayerStatus.PREPARED ||
                _state.value.status == ServicePlayerStatus.PAUSED ||
                _state.value.status == ServicePlayerStatus.COMPLETED

        if (!canPlay) {
            Log.d(TAG, "play ignored, state=${_state.value.status}")
            return
        }

        if (_state.value.status == ServicePlayerStatus.COMPLETED) {
            try {
                player.seekTo(0)
            } catch (e: Exception) {
                Log.e(TAG, "seekTo error", e)
            }
        }

        try {
            player.start()
            Log.d(TAG, "MediaPlayer started")

            _state.value = _state.value.copy(
                status = ServicePlayerStatus.PLAYING,
                errorMessage = null
            )

            startProgressUpdates()
        } catch (e: Exception) {
            Log.e(TAG, "player.start() failed", e)
            _state.value = _state.value.copy(
                status = ServicePlayerStatus.ERROR,
                errorMessage = e.message
            )
        }
    }

    override fun pause() {
        val player = mediaPlayer
        Log.d(TAG, "pause called, playerNull=${player == null}")

        if (player == null) return

        try {
            if (player.isPlaying) {
                player.pause()
                Log.d(TAG, "MediaPlayer paused")
            }

            stopProgressUpdates()
            exitForeground()

            _state.value = _state.value.copy(
                status = ServicePlayerStatus.PAUSED,
                currentPosition = player.currentPosition,
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "pause failed", e)
        }
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    override fun enterForeground() {
        Log.d(TAG, "enterForeground called, isPlaying=${isPlaying()}")

        if (!isPlaying()) return

        val notification = buildNotification()

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    0
                }
            )
            Log.d(TAG, "enterForeground success")
        } catch (e: Exception) {
            Log.e(TAG, "enterForeground failed", e)
        }
    }

    override fun exitForeground() {
        Log.d(TAG, "exitForeground called")
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun stopServicePlayback() {
        Log.d(TAG, "stopServicePlayback called")
        pause()
        exitForeground()
        releasePlayer()
        stopSelf()
    }

    override fun stateFlow(): StateFlow<ServicePlayerState> = _state

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Playlist Maker")
            .setContentText("$artistName - $trackName")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isPlaying()) {
                _state.value = _state.value.copy(
                    status = ServicePlayerStatus.PLAYING,
                    currentPosition = mediaPlayer?.currentPosition ?: 0
                )
                delay(PROGRESS_UPDATE_DELAY_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        Log.d(TAG, "releasePlayer")
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null

        _state.value = ServicePlayerState(
            status = ServicePlayerStatus.IDLE,
            currentPosition = 0,
            duration = 0,
            errorMessage = null
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}