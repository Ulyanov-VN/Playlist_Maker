package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.BottomSheetAddToPlaylistBinding
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.playlist.ui.adapters.PlaylistBottomSheetAdapter
import com.example.playlistmaker.search.domain.entity.Track
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaylistBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlistAdapter: PlaylistBottomSheetAdapter
    private var onPlaylistSelected: ((Playlist) -> Unit)? = null
    private var onNewPlaylistClicked: (() -> Unit)? = null
    private var playlists: List<Playlist> = emptyList()

    companion object {
        private const val TAG = "PlaylistBottomSheet"

        fun show(
            fragmentManager: FragmentManager,
            playlists: List<Playlist>,
            onPlaylistSelected: (Playlist) -> Unit,
            onNewPlaylistClicked: () -> Unit
        ) {
            val fragment = PlaylistBottomSheetFragment().apply {
                this.playlists = playlists
                this.onPlaylistSelected = onPlaylistSelected
                this.onNewPlaylistClicked = onNewPlaylistClicked
            }
            fragment.show(fragmentManager, TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        updateUI()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistBottomSheetAdapter(
            playlists = playlists
        ) { playlist ->
            onPlaylistSelected?.invoke(playlist)
            dismiss()
        }

        binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecyclerView.adapter = playlistAdapter
    }

    private fun setupListeners() {
        binding.newPlaylistButton.setOnClickListener {
            onNewPlaylistClicked?.invoke()
            dismiss()
        }
    }

    private fun updateUI() {
        if (playlists.isNotEmpty()) {
            binding.divider.visibility = View.VISIBLE
            binding.playlistsRecyclerView.visibility = View.VISIBLE
            binding.title.text = getString(R.string.add_to_playlist)
        } else {
            binding.divider.visibility = View.GONE
            binding.playlistsRecyclerView.visibility = View.GONE
            binding.title.text = getString(R.string.no_playlists_yet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}