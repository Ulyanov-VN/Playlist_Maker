package com.example.playlistmaker.player.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.viewmodels.PlayerState
import com.example.playlistmaker.player.ui.viewmodels.PlayerViewModel
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.ui.SearchActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_player)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.pausePlayback()
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

        val track = if (intent.hasExtra(TRACK_EXTRA)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(TRACK_EXTRA, Track::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(TRACK_EXTRA)
            }
        } else {
            null
        }

        track?.let {
            viewModel.initialize(it)
            setupBackButton()
            bindTrackData(it)
            setupPlayPauseButton()
            setupOtherButtons()
            observePlayerState()
        } ?: run {
            finish()
        }
    }

    private fun observePlayerState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PlayerState.Playing -> {
                        updatePlayPauseButton(true)
                        updateCurrentTime(state.currentPosition)
                    }
                    is PlayerState.Paused -> {
                        updatePlayPauseButton(false)
                        updateCurrentTime(state.currentPosition)
                    }
                    is PlayerState.Stopped -> {
                        updatePlayPauseButton(false)
                        findViewById<TextView>(R.id.trackDuration).text = "00:00"
                    }
                    is PlayerState.Prepared -> {
                        findViewById<TextView>(R.id.durationValue).text = state.trackDuration
                    }
                    is PlayerState.Error -> {
                        showErrorState()
                    }
                }
            }
        }
    }

    private fun showErrorState() {
        findViewById<ImageButton>(R.id.playPauseButton).isEnabled = false
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            viewModel.pausePlayback()
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

        val coverUrl = viewModel.getCoverArtwork(track.artworkUrl100)

        Glide.with(this)
            .load(coverUrl)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .into(artwork)

        findViewById<TextView>(R.id.trackName).text = track.trackName ?: getString(R.string.unknown_track)
        findViewById<TextView>(R.id.artistName).text = track.artistName ?: getString(R.string.unknown_artist)
        findViewById<TextView>(R.id.albumValue).text = track.collectionName ?: getString(R.string.unknown_album)

        val releaseYear = viewModel.getReleaseYear(track.releaseDate)
        findViewById<TextView>(R.id.yearValue).text = releaseYear ?: getString(R.string.unknown_year)

        findViewById<TextView>(R.id.genreValue).text = track.primaryGenreName ?: getString(R.string.unknown_genre)

        val countryName = viewModel.getCountryName(track.country)
        findViewById<TextView>(R.id.countryValue).text = countryName
    }

    private fun setupPlayPauseButton() {
        findViewById<ImageButton>(R.id.playPauseButton).setOnClickListener {
            viewModel.togglePlayPause()
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
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
            viewModel.togglePlaylistState()
            updatePlaylistButton(viewModel.isInPlaylist)
        }

        findViewById<ImageButton>(R.id.favoriteButton).setOnClickListener {
            viewModel.toggleFavoriteState()
            updateFavoriteButton(viewModel.isFavorite)
        }

        updateFavoriteButton(viewModel.isFavorite)
        updatePlaylistButton(viewModel.isInPlaylist)
    }

    private fun updatePlaylistButton(isInPlaylist: Boolean) {
        val addToPlaylistButton = findViewById<ImageButton>(R.id.addToPlaylistButton)
        val iconResId = if (isInPlaylist) {
            R.drawable.ic_add_to_playlist
        } else {
            R.drawable.ic_playlist
        }
        addToPlaylistButton.setImageResource(iconResId)
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        findViewById<ImageButton>(R.id.favoriteButton).setImageResource(
            if (isFavorite) R.drawable.ic_favorite_border_add else R.drawable.ic_favorite_border
        )
    }

    private fun updateCurrentTime(currentPosition: Int) {
        findViewById<TextView>(R.id.trackDuration).text = viewModel.formatTime(currentPosition.toLong())
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
    }
}