package com.example.playlistmaker.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.compose.MediaScreen
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.search.domain.entity.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment() {

    private val favoriteTracksViewModel: FavoriteTracksViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        playlistsViewModel.refreshPlaylists()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MediaScreen(
                    favoriteTracksViewModel = favoriteTracksViewModel,
                    playlistsViewModel = playlistsViewModel,
                    onTrackClick = ::openPlayer,
                    onCreatePlaylistClick = {
                        findNavController().navigate(R.id.action_media_to_create_playlist)
                    },
                    onPlaylistClick = { playlist ->
                        val bundle = Bundle().apply {
                            putLong("playlistId", playlist.playlistId)
                        }
                        findNavController().navigate(R.id.action_media_to_playlistDetails, bundle)
                    }
                )
            }
        }
    }

    private fun openPlayer(track: Track) {
        val bundle = bundleOf("track" to track)
        findNavController().navigate(R.id.playerFragment, bundle)
    }
}