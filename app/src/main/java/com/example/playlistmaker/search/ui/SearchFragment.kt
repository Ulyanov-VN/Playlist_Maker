package com.example.playlistmaker.search.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.ui.compose.SearchScreen
import com.example.playlistmaker.search.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()

    private var clickJob: Job? = null
    private var isClickAllowed = true

    private var historyTracks by mutableStateOf<List<Track>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshHistory()
    }

    override fun onResume() {
        super.onResume()
        refreshHistory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SearchScreen(
                    stateFlow = viewModel.state,
                    history = historyTracks,
                    formatTrackTime = viewModel::formatTime,
                    onSearch = viewModel::search,
                    onRetry = viewModel::retry,
                    onClearSearchState = {
                        viewModel.clearState()
                        refreshHistory()
                    },
                    onClearHistory = {
                        viewModel.clearHistory()
                        refreshHistory()
                    },
                    onTrackClick = { track: Track ->
                        viewModel.saveTrackToHistory(track)
                        refreshHistory()
                        openPlayer(track)
                    }
                )
            }
        }
    }

    private fun refreshHistory() {
        historyTracks = viewModel.getHistory()
    }

    private fun openPlayer(track: Track) {
        if (!clickDebounce()) return

        val bundle = bundleOf("track" to track)
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed

        if (isClickAllowed) {
            isClickAllowed = false
            clickJob?.cancel()
            clickJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }

        return current
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clickJob?.cancel()
        isClickAllowed = true
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}