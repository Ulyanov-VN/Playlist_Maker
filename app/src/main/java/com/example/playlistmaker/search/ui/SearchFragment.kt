package com.example.playlistmaker.search.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.ui.adapters.TrackAdapter
import com.example.playlistmaker.search.ui.viewmodels.SearchUiState
import com.example.playlistmaker.search.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.activity_search) {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var contentContainer: FrameLayout
    private lateinit var progressBar: ProgressBar

    private val viewModel: SearchViewModel by viewModel()

    private var searchJob: Job? = null
    private var clickJob: Job? = null
    private var isClickAllowed = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        binding = ActivitySearchBinding.bind(view)

        initViews(view)
        setupSearchField()
        setupResultsList()

        if (searchEditText.text.isEmpty()) bindHistory()

        observeViewModelState()
    }

    override fun onResume() {
        super.onResume()
        if (this::searchEditText.isInitialized && searchEditText.text.isNullOrEmpty()) {
            bindHistory()
        }
    }

    private fun initViews(root: View) {
        searchEditText = root.findViewById(R.id.searchEditText)
        clearButton = root.findViewById(R.id.clearButton)
        recyclerView = root.findViewById(R.id.recyclerView)
        historyRecyclerView = root.findViewById(R.id.historyRecyclerView)
        contentContainer = root.findViewById(R.id.mainContent)

        progressBar = ProgressBar(requireContext()).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }
            indeterminateTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue))
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModelState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                historyRecyclerView.visibility = View.GONE
                recyclerView.visibility = View.GONE
                contentContainer.removeAllViews()

                when (state) {
                    is SearchUiState.Empty -> {
                        if (searchEditText.text.isEmpty()) bindHistory()
                    }
                    is SearchUiState.Loading -> {
                        trackAdapter.updateTracks(emptyList())
                        contentContainer.addView(progressBar)
                    }
                    is SearchUiState.Success -> showSearchResults(state.tracks)
                    is SearchUiState.NoResults -> showEmptyView()
                    is SearchUiState.Error -> showErrorView()
                }
            }
        }
    }

    private fun showSearchResults(tracks: List<Track>) {
        recyclerView.visibility = View.VISIBLE
        historyRecyclerView.visibility = View.GONE
        trackAdapter.updateTracks(tracks)
        recyclerView.bringToFront()
    }

    private fun showEmptyView() {
        trackAdapter.updateTracks(emptyList())
        val emptyView = layoutInflater.inflate(R.layout.placeholder_empty, contentContainer, false)
        contentContainer.addView(emptyView)
    }

    private fun showErrorView() {
        trackAdapter.updateTracks(emptyList())
        val errView = layoutInflater.inflate(R.layout.placeholder_error, contentContainer, false)
        errView.findViewById<Button>(R.id.btnRetry).setOnClickListener {
            viewModel.retry()
        }
        contentContainer.addView(errView)
    }

    private fun openPlayer(track: Track) {
        if (!clickDebounce()) return

        // отменяем отложенный поиск (аналог removeCallbacks)
        searchJob?.cancel()

        hideKeyboard()
        searchEditText.clearFocus()

        val bundle = bundleOf("track" to track)
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    private fun scheduleSearchDebounced(query: String) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            if (query.isNotEmpty()) {
                viewModel.search(query)
            }
        }
    }

    private fun bindHistory() {
        val hist = viewModel.getHistory()
        if (hist.isEmpty()) {
            historyRecyclerView.visibility = View.GONE
            return
        }

        val headerAdapter = object : RecyclerView.Adapter<TextViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                TextViewHolder(layoutInflater.inflate(R.layout.item_history_header, parent, false))

            override fun getItemCount() = 1

            override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
                holder.textView.text = getString(R.string.history_title)
            }
        }

        val tracksAdapter = TrackAdapter(
            tracks = hist,
            format = { millis -> viewModel.formatTime(millis) }
        ) { track ->
            onHistoryItemClick(track)
            openPlayer(track)
        }

        val footerAdapter = object : RecyclerView.Adapter<ButtonViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ButtonViewHolder(layoutInflater.inflate(R.layout.item_history_footer, parent, false)).apply {
                    button.setOnClickListener {
                        viewModel.clearHistory()
                        bindHistory()
                    }
                }

            override fun getItemCount() = 1
            override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {}
        }

        historyRecyclerView.adapter = ConcatAdapter(headerAdapter, tracksAdapter, footerAdapter)
        historyRecyclerView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        contentContainer.removeAllViews()
        historyRecyclerView.bringToFront()
    }

    private fun setupResultsList() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(
            tracks = emptyList(),
            format = { millis -> viewModel.formatTime(millis) }
        ) { track ->
            viewModel.saveTrackToHistory(track)
            openPlayer(track)
        }
        recyclerView.adapter = trackAdapter
    }

    private fun setupSearchField() {
        searchEditText.setOnClickListener { showKeyboard() }

        clearButton.setOnClickListener {
            searchEditText.text?.clear()
            hideKeyboard()
            clearButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            viewModel.clearState()
            trackAdapter.updateTracks(emptyList())

            // отменяем отложенный поиск
            searchJob?.cancel()

            if (searchEditText.hasFocus()) bindHistory()
        }

        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val empty = s.isNullOrEmpty()
                clearButton.visibility = if (empty) View.GONE else View.VISIBLE

                if (empty && searchEditText.hasFocus()) {
                    bindHistory()
                    recyclerView.visibility = View.GONE
                    searchJob?.cancel()
                } else {
                    historyRecyclerView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (!s.isNullOrEmpty()) {
                        scheduleSearchDebounced(s.toString())
                    }
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        searchEditText.addTextChangedListener(textWatcher)

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchJob?.cancel()
                viewModel.search(searchEditText.text.toString())
                hideKeyboard()
                true
            } else false
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            val isEmpty = searchEditText.text.isNullOrEmpty()
            if (hasFocus && isEmpty) {
                bindHistory()
            } else {
                historyRecyclerView.visibility = View.GONE
            }
        }
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

    private fun onHistoryItemClick(track: Track) {
        viewModel.saveTrackToHistory(track)
    }

    private fun showKeyboard() {
        searchEditText.post {
            searchEditText.requestFocus()
            WindowCompat.getInsetsController(requireActivity().window, searchEditText)
                ?.show(WindowInsetsCompat.Type.ime())
        }
    }

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(requireActivity().window, searchEditText)
            ?.hide(WindowInsetsCompat.Type.ime())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        clickJob?.cancel()
        isClickAllowed = true
    }

    private class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewHeader)
    }

    private class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.clearHistoryFooter)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}