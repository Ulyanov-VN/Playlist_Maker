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
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var playbackPosition = 0
    private var isFavorite = false
    private var isInPlaylist = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_player)

        findViewById<TextView>(R.id.trackDuration).text = "00:00"

        val track = if (intent.hasExtra(TRACK_EXTRA)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(TRACK_EXTRA, Track::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(TRACK_EXTRA)
            }
        } else {
            null
        } ?: return

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
    }

    private fun initMediaPlayer(track: Track) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    findViewById<TextView>(R.id.durationValue).text = track.trackTimeMillis?.let {
                        formatTime(it)
                    } ?: "--:--"
                    findViewById<TextView>(R.id.trackDuration).text = "00:00"
                    if (playbackPosition > 0) seekTo(playbackPosition)
                }
                setOnCompletionListener {
                    stopPlayback()
                    updatePlayPauseButton()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
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

        // Преобразование кода страны в название
        val countryName = track.country?.let { countryCode ->
            try {
                // Получаем локали для страны
                val locale = Locale("", countryCode)
                // Получаем название страны на языке системы
                locale.getDisplayCountry(Locale.getDefault()).takeIf { it.isNotBlank() }
                    ?: countryCode // Если название пустое, возвращаем код
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error converting country code: $countryCode", e)
                countryCode // В случае ошибки возвращаем исходный код
            }
        } ?: getString(R.string.unknown_country) // Если countryCode == null

        findViewById<TextView>(R.id.countryValue).text = countryName
    }

    private fun setupPlayPauseButton() {
        findViewById<ImageButton>(R.id.playPauseButton).setOnClickListener {
            togglePlayPause()
        }
        updatePlayPauseButton()
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

            // Дополнительная логика (можно добавить анимацию или Toast)
            if (isInPlaylist) {
                // Трек добавлен в плейлист
                // showToast("Добавлено в плейлист")
            } else {
                // Трек удален из плейлиста
                // showToast("Удалено из плейлиста")
            }

            // TODO: Реализовать логику добавления/удаления из плейлиста в базу данных
        }

        findViewById<ImageButton>(R.id.favoriteButton).setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteButton()
            // TODO: Добавить логику сохранения в избранное
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

    private fun togglePlayPause() {
        if (isPlaying) pausePlayback() else startPlayback()
    }

    private fun startPlayback() {
        mediaPlayer?.let {
            it.start()
            isPlaying = true
            updatePlayPauseButton()
            handler.post(updateTimeRunnable)
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
            it.reset()
            isPlaying = false
            playbackPosition = 0
            handler.removeCallbacks(updateTimeRunnable)
            // Устанавливаем время 00:00 при остановке
            findViewById<TextView>(R.id.trackDuration).text = "00:00"
            updatePlayPauseButton()
        }
    }

    private fun updateCurrentTime() {
        mediaPlayer?.let { mp ->
            findViewById<TextView>(R.id.trackDuration).text = formatTime(mp.currentPosition.toLong())
        }
    }

    private fun formatTime(millis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
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
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(TRACK_EXTRA, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(TRACK_EXTRA)
        }
        outState.putParcelable(TRACK_EXTRA, track)
        outState.putBoolean(PLAYBACK_STATE, isPlaying)
        outState.putInt(PLAYBACK_POSITION, playbackPosition)
        outState.putBoolean(FAVORITE_STATE, isFavorite)
        outState.putBoolean(PLAYLIST_STATE, isInPlaylist)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(TRACK_EXTRA, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState.getParcelable(TRACK_EXTRA)
        }
        track?.let { bindTrackData(it) }

        isPlaying = savedInstanceState.getBoolean(PLAYBACK_STATE, false)
        playbackPosition = savedInstanceState.getInt(PLAYBACK_POSITION, 0)
        isFavorite = savedInstanceState.getBoolean(FAVORITE_STATE, false)
        isInPlaylist = savedInstanceState.getBoolean(PLAYLIST_STATE, false)

        updateFavoriteButton()
        updatePlaylistButton()

        if (isPlaying) startPlayback()
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
        const val PLAYBACK_STATE = "playback_state"
        const val PLAYBACK_POSITION = "playback_position"
        const val FAVORITE_STATE = "favorite_state"
        const val PLAYLIST_STATE = "playlist_state"
        private const val TIME_UPDATE_INTERVAL = 500L
    }
}