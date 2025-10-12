package com.example.playlistmaker.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.di.Creator
import com.example.playlistmaker.domain.entity.Track
import com.example.playlistmaker.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.domain.interactor.GetCountryNameInteractor
import com.example.playlistmaker.domain.interactor.GetCoverArtworkInteractor
import com.example.playlistmaker.domain.interactor.GetReleaseYearInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractor

class PlayerActivity : AppCompatActivity() {

    private lateinit var playerInteractor: PlayerInteractor
    private lateinit var formatTimeInteractor: FormatTimeInteractor
    private lateinit var getCountryNameInteractor: GetCountryNameInteractor
    private lateinit var getCoverArtworkInteractor: GetCoverArtworkInteractor
    private lateinit var getReleaseYearInteractor: GetReleaseYearInteractor

    private var isPlaying = false
    private var isFavorite = false
    private var isInPlaylist = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable
    private var currentTrack: Track? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_player)

        // Инициализация Interactors через Creator
        playerInteractor = Creator.providePlayerInteractor()
        formatTimeInteractor = Creator.provideFormatTimeInteractor()
        getCountryNameInteractor = Creator.provideGetCountryNameInteractor()
        getCoverArtworkInteractor = Creator.provideGetCoverArtworkInteractor()
        getReleaseYearInteractor = Creator.provideGetReleaseYearInteractor()

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

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
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
            initPlayer(track)
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
            finish()
        }
    }

    private fun initPlayer(track: Track) {
        playerInteractor.setOnPreparedListener {
            findViewById<TextView>(R.id.durationValue).text =
                formatTimeInteractor.executeForTrack(track.trackTimeMillis)
            findViewById<TextView>(R.id.trackDuration).text = "00:00"

            if (isPlaying) {
                startPlayback()
            }
        }

        playerInteractor.setOnCompletionListener {
            onPlaybackComplete()
        }

        playerInteractor.setOnErrorListener { errorMessage ->
            showErrorState()
        }

        playerInteractor.initialize(track)
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

        val coverUrl = getCoverArtworkInteractor.execute(track.artworkUrl100)

        Glide.with(this)
            .load(coverUrl)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .into(artwork)

        findViewById<TextView>(R.id.trackName).text = track.trackName ?: getString(R.string.unknown_track)
        findViewById<TextView>(R.id.artistName).text = track.artistName ?: getString(R.string.unknown_artist)
        findViewById<TextView>(R.id.albumValue).text = track.collectionName ?: getString(R.string.unknown_album)

        val releaseYear = getReleaseYearInteractor.execute(track.releaseDate)
        findViewById<TextView>(R.id.yearValue).text = releaseYear ?: getString(R.string.unknown_year)

        findViewById<TextView>(R.id.genreValue).text = track.primaryGenreName ?: getString(R.string.unknown_genre)

        val countryName = getCountryNameInteractor.execute(track.country)
        findViewById<TextView>(R.id.countryValue).text = countryName
    }

    private fun setupPlayPauseButton() {
        findViewById<ImageButton>(R.id.playPauseButton).setOnClickListener {
            togglePlayPause()
        }
        updatePlayPauseButton()
    }

    private fun togglePlayPause() {
        if (!playerInteractor.isPrepared()) return

        if (isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        playerInteractor.play()
        isPlaying = true
        updatePlayPauseButton()
        handler.post(updateTimeRunnable)
    }

    private fun pausePlayback() {
        playerInteractor.pause()
        isPlaying = false
        updatePlayPauseButton()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun stopPlayback() {
        playerInteractor.pause()
        isPlaying = false
        updatePlayPauseButton()
        handler.removeCallbacks(updateTimeRunnable)
        findViewById<TextView>(R.id.trackDuration).text = "00:00"
    }

    private fun onPlaybackComplete() {
        isPlaying = false
        handler.removeCallbacks(updateTimeRunnable)
        findViewById<TextView>(R.id.trackDuration).text = "00:00"
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
        if (playerInteractor.isPrepared()) {
            val currentPos = playerInteractor.getCurrentPosition()
            findViewById<TextView>(R.id.trackDuration).text = formatTimeInteractor.execute(currentPos.toLong())

            if (currentPos >= playerInteractor.getDuration() - 100) {
                onPlaybackComplete()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerInteractor.release()
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentTrack?.let {
            outState.putParcelable(TRACK_EXTRA, it)
        }
        outState.putBoolean("is_playing", isPlaying)
        outState.putBoolean("is_favorite", isFavorite)
        outState.putBoolean("is_in_playlist", isInPlaylist)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        currentTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(TRACK_EXTRA, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState.getParcelable(TRACK_EXTRA)
        }

        isPlaying = savedInstanceState.getBoolean("is_playing", false)
        isFavorite = savedInstanceState.getBoolean("is_favorite", false)
        isInPlaylist = savedInstanceState.getBoolean("is_in_playlist", false)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreState(savedInstanceState)

        currentTrack?.let { track ->
            bindTrackData(track)
            updateFavoriteButton()
            updatePlaylistButton()
            updatePlayPauseButton()
        }
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
        private const val TIME_UPDATE_INTERVAL = 500L
    }
}