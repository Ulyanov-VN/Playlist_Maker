package com.example.playlistmaker.playlist.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.BottomSheetAddToPlaylistBinding
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.playlist.ui.adapters.PlaylistBottomSheetAdapter
import com.example.playlistmaker.search.domain.entity.Track
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddToPlaylistBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_TRACK = "track"
        private const val ARG_PLAYLISTS = "playlists"

        fun newInstance(
            track: Track,
            playlists: List<Playlist>,
            onNewPlaylistClick: () -> Unit,
            onPlaylistSelected: (Playlist) -> Unit
        ): AddToPlaylistBottomSheet {
            val args = Bundle().apply {
                putParcelable(ARG_TRACK, track)
                putParcelableArrayList(ARG_PLAYLISTS, ArrayList(playlists))
            }
            val fragment = AddToPlaylistBottomSheet()
            fragment.arguments = args
            fragment.onNewPlaylistClickCallback = onNewPlaylistClick
            fragment.onPlaylistSelectedCallback = onPlaylistSelected
            return fragment
        }
    }

    private var _binding: BottomSheetAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlistAdapter: PlaylistBottomSheetAdapter
    private var onNewPlaylistClickCallback: (() -> Unit)? = null
    private var onPlaylistSelectedCallback: ((Playlist) -> Unit)? = null

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

        val track = arguments?.getParcelable<Track>(ARG_TRACK)
        val playlists = arguments?.getParcelableArrayList<Playlist>(ARG_PLAYLISTS) ?: emptyList()

        setupRecyclerView(playlists)
        setupListeners()
        updateUI(playlists)
    }

    private fun setupRecyclerView(playlists: List<Playlist>) {
        playlistAdapter = PlaylistBottomSheetAdapter(
            playlists = playlists
        ) { playlist ->
            onPlaylistSelectedCallback?.invoke(playlist)
            dismiss()
        }

        binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecyclerView.adapter = playlistAdapter
    }

    private fun setupListeners() {
        binding.newPlaylistButton.setOnClickListener {
            onNewPlaylistClickCallback?.invoke()
            dismiss()
        }
    }

    private fun updateUI(playlists: List<Playlist>) {
        playlistAdapter.updatePlaylists(playlists)

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
        onNewPlaylistClickCallback = null
        onPlaylistSelectedCallback = null
    }
}