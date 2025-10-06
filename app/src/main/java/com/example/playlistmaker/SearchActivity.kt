package com.example.playlistmaker

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.data.history.SearchHistory
import com.example.playlistmaker.ui.search.SearchUiState
import com.example.playlistmaker.ui.search.SearchViewModel
import com.example.playlistmaker.ui.search.SearchViewModelFactory
import kotlinx.coroutines.launch

class SearchActivity : BaseActivity() {

    companion object {
        const val EXTRA_FROM_SEARCH = "from_search"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun getLayoutId() = R.layout.activity_search

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var contentContainer: FrameLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var searchHistory: SearchHistory
    private val viewModel: SearchViewModel by viewModels { SearchViewModelFactory() }

    private val searchHandler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        val query = searchEditText.text.toString()
        if (query.isNotEmpty()) {
            viewModel.search(query)
        }
    }

    private var isClickAllowed = true
    private val clickHandler = Handler(Looper.getMainLooper())

    private val playerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val fromSearch = data?.getBooleanExtra(EXTRA_FROM_SEARCH, false) ?: false
        if (fromSearch) {
            viewModel.getLastSearchTerm()?.let { term ->
                searchEditText.setText(term)
                searchEditText.setSelection(term.length)
            }
        } else {
            searchEditText.text?.clear()
            bindHistory()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(getLayoutId())

        initViews()
        setupSearchField()
        setupResultsList()

        if (searchEditText.text.isEmpty()) bindHistory()

        observeViewModelState()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.recyclerView)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        contentContainer = findViewById(R.id.mainContent)

        findViewById<ImageButton>(R.id.icon_button).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(360, 640).apply {
                gravity = Gravity.CENTER


            }
            indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(this@SearchActivity, R.color.blue))
        }

        searchHistory = SearchHistory(
            getSharedPreferences("search_history_prefs", MODE_PRIVATE)
        )
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
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
        val emptyView = layoutInflater.inflate(
            R.layout.placeholder_empty, contentContainer, false
        )
        contentContainer.addView(emptyView)
    }

    private fun showErrorView() {
        trackAdapter.updateTracks(emptyList())
        val errView = layoutInflater.inflate(
            R.layout.placeholder_error, contentContainer, false
        )
        errView.findViewById<Button>(R.id.btnRetry).setOnClickListener {
            viewModel.retry()
        }
        contentContainer.addView(errView)
    }

    private fun openPlayer(track: Track, fromSearch: Boolean) {
        if (!clickDebounce()) return

        val intent = Intent(this, PlayerActivity::class.java).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putExtra(PlayerActivity.TRACK_EXTRA, track)
            } else {
                @Suppress("DEPRECATION")
                putExtra(PlayerActivity.TRACK_EXTRA, track)
            }
            putExtra(EXTRA_FROM_SEARCH, fromSearch)
        }
        playerLauncher.launch(intent)
    }

    private fun bindHistory() {
        val hist = searchHistory.getHistory()
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

        val tracksAdapter = TrackAdapter(hist) { track ->
            onHistoryItemClick(track)
            openPlayer(track, fromSearch = false)
        }

        val footerAdapter = object : RecyclerView.Adapter<ButtonViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ButtonViewHolder(layoutInflater.inflate(R.layout.item_history_footer, parent, false))
                    .apply {
                        button.setOnClickListener {
                            searchHistory.clearHistory()
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(emptyList()) { track ->
            searchHistory.saveTrack(track)
            openPlayer(track, fromSearch = true)
        }
        recyclerView.adapter = trackAdapter
    }

    private fun setupSearchField() {
        searchEditText.setOnClickListener {
            showKeyboard()
        }

        clearButton.setOnClickListener {
            searchEditText.text?.clear()
            hideKeyboard()
            clearButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            viewModel.clearState()
            trackAdapter.updateTracks(emptyList())
            searchHandler.removeCallbacks(searchRunnable)
            if (searchEditText.hasFocus()) bindHistory()
        }

        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            private var currentText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                currentText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val empty = s.isNullOrEmpty()
                clearButton.visibility = if (empty) View.GONE else View.VISIBLE

                if (empty && searchEditText.hasFocus()) {
                    bindHistory()
                    recyclerView.visibility = View.GONE
                    searchHandler.removeCallbacks(searchRunnable)
                } else {
                    historyRecyclerView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (!s.isNullOrEmpty()) {
                        searchHandler.removeCallbacks(searchRunnable)
                        searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
                    }
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                val newText = s?.toString() ?: ""
                if (newText != currentText) {
                    trackAdapter.updateTracks(emptyList())
                }
            }
        })

        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnter = event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                    event.action == android.view.KeyEvent.ACTION_DOWN
            if (isSearch || isEnter) {
                searchHandler.removeCallbacks(searchRunnable)
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
            clickHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun onHistoryItemClick(track: Track) {
        searchHistory.saveTrack(track)
        searchEditText.setText(track.trackName)
        searchEditText.setSelection(searchEditText.text?.length ?: 0)
        searchHandler.removeCallbacks(searchRunnable)
        viewModel.search(track.trackName.orEmpty())
    }

    private fun showKeyboard() {
        searchEditText.post {
            searchEditText.requestFocus()
            WindowCompat.getInsetsController(window, searchEditText)
                .show(WindowInsetsCompat.Type.ime())
        }
    }

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(window, searchEditText)
            .hide(WindowInsetsCompat.Type.ime())
    }

    override fun onDestroy() {
        super.onDestroy()
        searchHandler.removeCallbacks(searchRunnable)
        clickHandler.removeCallbacksAndMessages(null)
    }

    private class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewHeader)
    }

    private class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.clearHistoryFooter)
    }
}