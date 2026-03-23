package com.example.playlistmaker.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoriteTracksBinding
import com.example.playlistmaker.favorites.ui.adapters.FavoriteTracksAdapter
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksState
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksViewModel
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoriteTracksViewModel by viewModel()

    private lateinit var favoriteTracksAdapter: FavoriteTracksAdapter

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        favoriteTracksAdapter = FavoriteTracksAdapter(
            tracks = emptyList()
        ) { track ->
            openPlayer(track)
        }

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = favoriteTracksAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is FavoriteTracksState.Empty -> showEmptyState()
                    is FavoriteTracksState.Content -> showContent(state.tracks)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.tracksRecyclerView.visibility = View.GONE
        binding.iconSmile.visibility = View.VISIBLE
        binding.emptyText.visibility = View.VISIBLE
        binding.emptyText.text = getString(R.string.text_favorite_tracks)
    }

    private fun showContent(tracks: List<Track>) {
        binding.tracksRecyclerView.visibility = View.VISIBLE
        binding.iconSmile.visibility = View.GONE
        binding.emptyText.visibility = View.GONE

        favoriteTracksAdapter.updateTracks(tracks)
    }

    private fun openPlayer(track: Track) {
        val bundle = bundleOf("track" to track)
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}