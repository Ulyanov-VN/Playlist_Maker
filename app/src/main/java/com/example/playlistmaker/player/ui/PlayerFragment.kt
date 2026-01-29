package com.example.playlistmaker.player.ui

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.viewmodels.PlayerState
import com.example.playlistmaker.player.ui.viewmodels.PlayerStatus
import com.example.playlistmaker.player.ui.viewmodels.PlayerViewModel
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment(R.layout.activity_player) {

    private val viewModel: PlayerViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )

        val track: Track? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }

        if (track == null) {
            findNavController().popBackStack()
            return
        }

        viewModel.initialize(track)

        setupBackHandling(view)
        bindTrackData(view, track)
        setupPlayPauseButton(view)
        setupFavoriteButton(view)
        setupOtherButtons(view)
        observePlayerState(view)
    }

    private fun setupBackHandling(view: View) {
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            viewModel.pausePlayback()
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.pausePlayback()
                    findNavController().popBackStack()
                }
            }
        )
    }

    private fun observePlayerState(root: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->

                // Избранное всегда из общего стейта
                updateFavoriteButton(root, state.isFavorite)

                when (state.status) {
                    PlayerStatus.PLAYING -> {
                        updatePlayPauseButton(root, true)
                        updateCurrentTime(root, state.currentPosition)
                        root.findViewById<ImageButton>(R.id.playPauseButton).isEnabled = true
                    }

                    PlayerStatus.PAUSED -> {
                        updatePlayPauseButton(root, false)
                        updateCurrentTime(root, state.currentPosition)
                        root.findViewById<ImageButton>(R.id.playPauseButton).isEnabled = true
                    }

                    PlayerStatus.STOPPED -> {
                        updatePlayPauseButton(root, false)
                        // 00:00
                        updateCurrentTime(root, 0)
                        root.findViewById<ImageButton>(R.id.playPauseButton).isEnabled = true
                    }

                    PlayerStatus.PREPARED -> {
                        root.findViewById<TextView>(R.id.durationValue).text = state.trackDuration
                        // текущая позиция при подготовке — 00:00
                        updateCurrentTime(root, 0)
                        root.findViewById<ImageButton>(R.id.playPauseButton).isEnabled = true
                    }

                    PlayerStatus.ERROR -> {
                        root.findViewById<ImageButton>(R.id.playPauseButton).isEnabled = false
                    }
                }
            }
        }
    }

    private fun bindTrackData(root: View, track: Track) {
        val artwork = root.findViewById<ImageView>(R.id.albumArt)
        val isDarkTheme = isDarkTheme()
        val placeholderResId =
            if (isDarkTheme) R.drawable.placeholder_dark else R.drawable.placeholder_light

        val coverUrl = viewModel.getCoverArtwork(track.artworkUrl100)

        Glide.with(this)
            .load(coverUrl)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .into(artwork)

        root.findViewById<TextView>(R.id.trackName).text =
            track.trackName ?: getString(R.string.unknown_track)
        root.findViewById<TextView>(R.id.artistName).text =
            track.artistName ?: getString(R.string.unknown_artist)
        root.findViewById<TextView>(R.id.albumValue).text =
            track.collectionName ?: getString(R.string.unknown_album)

        val releaseYear = viewModel.getReleaseYear(track.releaseDate)
        root.findViewById<TextView>(R.id.yearValue).text =
            releaseYear ?: getString(R.string.unknown_year)

        root.findViewById<TextView>(R.id.genreValue).text =
            track.primaryGenreName ?: getString(R.string.unknown_genre)

        val countryName = viewModel.getCountryName(track.country)
        root.findViewById<TextView>(R.id.countryValue).text = countryName
    }

    private fun setupPlayPauseButton(root: View) {
        root.findViewById<ImageButton>(R.id.playPauseButton).setOnClickListener {
            viewModel.togglePlayPause()
        }
    }

    private fun setupFavoriteButton(root: View) {
        root.findViewById<ImageButton>(R.id.favoriteButton).setOnClickListener {
            viewModel.onFavoriteClicked()
        }
    }

    private fun updatePlayPauseButton(root: View, isPlaying: Boolean) {
        val isDarkTheme = isDarkTheme()
        val playIcon = if (isDarkTheme) R.drawable.play_night else R.drawable.play_day
        val pauseIcon = if (isDarkTheme) R.drawable.pause_night else R.drawable.pause_day

        root.findViewById<ImageButton>(R.id.playPauseButton).setImageResource(
            if (isPlaying) pauseIcon else playIcon
        )
    }

    private fun isDarkTheme(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupOtherButtons(root: View) {
        val addToPlaylistButton = root.findViewById<ImageButton>(R.id.addToPlaylistButton)

        addToPlaylistButton.setOnClickListener {
            // Здесь будет логика добавления в плейлист
        }
    }

    private fun updateFavoriteButton(root: View, isFavorite: Boolean) {
        root.findViewById<ImageButton>(R.id.favoriteButton).setImageResource(
            if (isFavorite) R.drawable.ic_favorite_border_add else R.drawable.ic_favorite_border
        )
    }

    private fun updateCurrentTime(root: View, currentPosition: Int) {
        root.findViewById<TextView>(R.id.trackDuration).text =
            viewModel.formatTime(currentPosition.toLong())
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.releasePlayer()
    }
}