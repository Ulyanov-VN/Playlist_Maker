package com.example.playlistmaker

import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var playbackPosition = 0
    private var isFavorite = false
    private var isInPlaylist = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable
    private var currentTrack: Track? = null
    private var isPrepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_player)

        // Обработка кнопки "Назад"
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopPlayback()
                val fromSearch = intent.getBooleanExtra(SearchActivity.EXTRA_FROM_SEARCH, false)
                if (fromSearch) {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(SearchActivity.EXTRA_FROM_SEARCH, true)
                    })
                }
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        findViewById<TextView>(R.id.trackDuration).text = "00:00"

        // Восстанавливаем состояние если есть
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Иначе получаем трек из intent
            currentTrack = if (intent.hasExtra(TRACK_EXTRA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(TRACK_EXTRA, Track::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(TRACK_EXTRA)
                }
            } else {
                null
            }
        }

        currentTrack?.let { track ->
            initMediaPlayer(track)
            setupBackButton()
            bindTrackData(track)
            setupPlayPauseButton()
            setupOtherButtons()

            updateTimeRunnable = object : Runnable {
                override fun run() {
                    updateCurrentTime()
                    handler.postDelayed(this, TIME_UPDATE_INTERVAL)
                }
            }
        } ?: run {
            Log.e("PlayerActivity", "No track provided")
            finish()
        }
    }

    private fun initMediaPlayer(track: Track) {
        try {
            val previewUrl = track.previewUrl
            if (previewUrl.isNullOrEmpty()) {
                Log.e("PlayerActivity", "Preview URL is null or empty")
                showErrorState()
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                setOnErrorListener { mp, what, extra ->
                    Log.e("PlayerActivity", "MediaPlayer error: what=$what, extra=$extra")
                    showErrorState()
                    false
                }
                prepareAsync()
                setOnPreparedListener {
                    isPrepared = true
                    findViewById<TextView>(R.id.durationValue).text = track.trackTimeMillis?.let {
                        formatTime(it)
                    } ?: "--:--"
                    findViewById<TextView>(R.id.trackDuration).text = "00:00"

                    // Восстанавливаем позицию воспроизведения после подготовки
                    if (playbackPosition > 0) {
                        seekTo(playbackPosition)
                    }

                    // Автоматически запускаем воспроизведение если было сохранено состояние playing
                    if (isPlaying) {
                        startPlayback()
                    }
                }
                setOnCompletionListener {
                    onPlaybackComplete()
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error initializing MediaPlayer", e)
            showErrorState()
        }
    }

    private fun showErrorState() {
        findViewById<ImageButton>(R.id.playPauseButton).isEnabled = false
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            stopPlayback()
            val fromSearch = intent.getBooleanExtra(SearchActivity.EXTRA_FROM_SEARCH, false)
            if (fromSearch) {
                setResult(RESULT_OK, Intent().apply {
                    putExtra(SearchActivity.EXTRA_FROM_SEARCH, true)
                })
            }
            finish()
        }
    }

    private fun bindTrackData(track: Track) {
        val artwork = findViewById<ImageView>(R.id.albumArt)
        val isDarkTheme = isDarkTheme()
        val placeholderResId = if (isDarkTheme) R.drawable.placeholder_dark else R.drawable.placeholder_light

        Glide.with(this)
            .load(track.artworkUrl100?.replace("100x100", "512x512"))
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .into(artwork)

        findViewById<TextView>(R.id.trackName).text = track.trackName ?: getString(R.string.unknown_track)
        findViewById<TextView>(R.id.artistName).text = track.artistName ?: getString(R.string.unknown_artist)
        findViewById<TextView>(R.id.albumValue).text = track.collectionName ?: getString(R.string.unknown_album)
        findViewById<TextView>(R.id.yearValue).text = track.releaseDate?.take(4) ?: getString(R.string.unknown_year)
        findViewById<TextView>(R.id.genreValue).text = track.primaryGenreName ?: getString(R.string.unknown_genre)

        val countryName = track.country?.let { countryCode ->
            try {
                val locale = Locale("", countryCode)
                locale.getDisplayCountry(Locale.getDefault()).takeIf { it.isNotBlank() } ?: countryCode
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error converting country code: $countryCode", e)
                countryCode
            }
        } ?: getString(R.string.unknown_country)

        findViewById<TextView>(R.id.countryValue).text = countryName
    }

    private fun setupPlayPauseButton() {
        findViewById<ImageButton>(R.id.playPauseButton).setOnClickListener {
            togglePlayPause()
        }
        updatePlayPauseButton()
    }

    private fun togglePlayPause() {
        if (!isPrepared) return

        if (isPlaying) {
            pausePlayback()
        } else {
            mediaPlayer?.let { mp ->
                // Если трек завершен или почти завершен, начинаем с начала
                if (mp.currentPosition >= mp.duration - 100 || playbackPosition >= mp.duration - 100) {
                    resetPlayback()
                }
            }
            startPlayback()
        }
    }

    private fun startPlayback() {
        mediaPlayer?.let {
            if (isPrepared) {
                it.start()
                isPlaying = true
                updatePlayPauseButton()
                handler.post(updateTimeRunnable)
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let {
            it.pause()
            isPlaying = false
            playbackPosition = it.currentPosition
            updatePlayPauseButton()
            handler.removeCallbacks(updateTimeRunnable)
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.let {
            it.stop()
            isPlaying = false
            playbackPosition = 0
            updatePlayPauseButton()
            handler.removeCallbacks(updateTimeRunnable)
            findViewById<TextView>(R.id.trackDuration).text = "00:00"
        }
    }

    private fun onPlaybackComplete() {
        mediaPlayer?.let {
            isPlaying = false
            playbackPosition = 0
            handler.removeCallbacks(updateTimeRunnable)
            findViewById<TextView>(R.id.trackDuration).text = "00:00"
            updatePlayPauseButton()
        }
    }

    private fun resetPlayback() {
        mediaPlayer?.let {
            it.seekTo(0)
            playbackPosition = 0
            findViewById<TextView>(R.id.trackDuration).text = "00:00"
        }
    }

    private fun updatePlayPauseButton() {
        val isDarkTheme = isDarkTheme()
        val playIcon = if (isDarkTheme) R.drawable.play_night else R.drawable.play_day
        val pauseIcon = if (isDarkTheme) R.drawable.pause_night else R.drawable.pause_day

        findViewById<ImageButton>(R.id.playPauseButton).setImageResource(
            if (isPlaying) pauseIcon else playIcon
        )
    }

    private fun isDarkTheme(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupOtherButtons() {
        val addToPlaylistButton = findViewById<ImageButton>(R.id.addToPlaylistButton)

        addToPlaylistButton.setOnClickListener {
            isInPlaylist = !isInPlaylist
            updatePlaylistButton()
        }

        findViewById<ImageButton>(R.id.favoriteButton).setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteButton()
        }

        updateFavoriteButton()
        updatePlaylistButton()
    }

    private fun updatePlaylistButton() {
        val addToPlaylistButton = findViewById<ImageButton>(R.id.addToPlaylistButton)
        val iconResId = if (isInPlaylist) {
            R.drawable.ic_add_to_playlist
        } else {
            R.drawable.ic_playlist
        }
        addToPlaylistButton.setImageResource(iconResId)
    }

    private fun updateFavoriteButton() {
        findViewById<ImageButton>(R.id.favoriteButton).setImageResource(
            if (isFavorite) R.drawable.ic_favorite_border_add else R.drawable.ic_favorite_border
        )
    }

    private fun updateCurrentTime() {
        mediaPlayer?.let { mp ->
            if (isPrepared) {
                val currentPos = mp.currentPosition
                findViewById<TextView>(R.id.trackDuration).text = formatTime(currentPos.toLong())

                // Автоматически останавливаем если достигли конца
                if (currentPos >= mp.duration - 100) {
                    onPlaybackComplete()
                }
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentTrack?.let {
            outState.putParcelable(TRACK_EXTRA, it)
        }
        outState.putBoolean(PLAYBACK_STATE, isPlaying)
        outState.putInt(PLAYBACK_POSITION, playbackPosition)
        outState.putBoolean(FAVORITE_STATE, isFavorite)
        outState.putBoolean(PLAYLIST_STATE, isInPlaylist)
        outState.putBoolean(PREPARED_STATE, isPrepared)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        currentTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(TRACK_EXTRA, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState.getParcelable(TRACK_EXTRA)
        }

        isPlaying = savedInstanceState.getBoolean(PLAYBACK_STATE, false)
        playbackPosition = savedInstanceState.getInt(PLAYBACK_POSITION, 0)
        isFavorite = savedInstanceState.getBoolean(FAVORITE_STATE, false)
        isInPlaylist = savedInstanceState.getBoolean(PLAYLIST_STATE, false)
        isPrepared = savedInstanceState.getBoolean(PREPARED_STATE, false)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreState(savedInstanceState)

        currentTrack?.let { track ->
            bindTrackData(track)
            updateFavoriteButton()
            updatePlaylistButton()

            if (!isPrepared) {
                initMediaPlayer(track)
            } else {

                updatePlayPauseButton()
            }
        }
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
        const val PLAYBACK_STATE = "playback_state"
        const val PLAYBACK_POSITION = "playback_position"
        const val FAVORITE_STATE = "favorite_state"
        const val PLAYLIST_STATE = "playlist_state"
        const val PREPARED_STATE = "prepared_state"
        private const val TIME_UPDATE_INTERVAL = 500L
    }
}