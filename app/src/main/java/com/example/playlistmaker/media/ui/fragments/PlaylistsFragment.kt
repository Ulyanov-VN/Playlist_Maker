package com.example.playlistmaker.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsState
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.playlist.ui.adapters.PlaylistAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistAdapter: PlaylistAdapter

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCreatePlaylistButton()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список плейлистов при каждом возвращении на экран
        viewModel.refreshPlaylists()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            playlists = emptyList()
        ) { playlist ->
            // TODO: Открыть экран плейлиста
        }

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.layoutManager = layoutManager
        binding.playlistsRecyclerView.adapter = playlistAdapter
    }

    private fun setupCreatePlaylistButton() {
        binding.createPlaylistButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_media_to_create_playlist)
            } catch (e: Exception) {
                parentFragment?.findNavController()?.navigate(R.id.action_media_to_create_playlist)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistsState.Empty -> showEmptyState()
                is PlaylistsState.Content -> showContent(state.playlists)
                else -> {
                    // Обработка других состояний
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.iconSmile.visibility = View.VISIBLE
        binding.emptyText.visibility = View.VISIBLE
    }

    private fun showContent(playlists: List<com.example.playlistmaker.playlist.domain.model.Playlist>) {
        binding.playlistsRecyclerView.visibility = View.VISIBLE
        binding.iconSmile.visibility = View.GONE
        binding.emptyText.visibility = View.GONE
        playlistAdapter.updatePlaylists(playlists)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}