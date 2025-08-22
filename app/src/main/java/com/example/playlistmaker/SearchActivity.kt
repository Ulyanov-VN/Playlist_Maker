package com.example.playlistmaker

import android.os.Bundle
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
import androidx.activity.viewModels
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*        // при повороте очищаем стейт и текст
        if (savedInstanceState != null) {
            viewModel.clearState()
            findViewById<EditText>(R.id.searchEditText).setText("")
        }*/

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(getLayoutId())

        // init views
        searchEditText      = findViewById(R.id.searchEditText)
        clearButton         = findViewById(R.id.clearButton)
        recyclerView        = findViewById(R.id.recyclerView)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        contentContainer    = findViewById(R.id.mainContent)

        val backBtn: ImageButton = findViewById(R.id.icon_button)
        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // или finish()
        }

        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(360, 640).apply {
                gravity = Gravity.CENTER
            }
        }

        // history
        searchHistory = SearchHistory(
            getSharedPreferences("search_history_prefs", MODE_PRIVATE)
        )
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        setupSearchField()
        setupResultsList()

        // показать историю при старте, если поле пусто
        if (searchEditText.text.isEmpty()) bindHistory()

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // прячем оба списка и контейнер
                historyRecyclerView.visibility = View.GONE
                recyclerView.visibility        = View.GONE
                contentContainer.removeAllViews()

                when (state) {
                    is SearchUiState.Empty -> {
                        if (searchEditText.text.isEmpty()) {
                            bindHistory()
                        }
                    }
                    is SearchUiState.Loading -> {
                        contentContainer.addView(progressBar)
                    }
                    is SearchUiState.Success -> {
                        recyclerView.visibility = View.VISIBLE
                        historyRecyclerView.visibility = View.GONE   // ← принудительно прячем историю

                        trackAdapter = TrackAdapter(state.tracks) { track ->
                            searchHistory.saveTrack(track)
                        }
                        recyclerView.adapter = trackAdapter
                        trackAdapter.filter(searchEditText.text.toString())
                        recyclerView.bringToFront()
                    }
                    is SearchUiState.NoResults -> {
                        val emptyView = layoutInflater.inflate(
                            R.layout.placeholder_empty, contentContainer, false
                        )
                        contentContainer.addView(emptyView)
                    }
                    is SearchUiState.Error -> {
                        val errView = layoutInflater.inflate(
                            R.layout.placeholder_error, contentContainer, false
                        )
                        errView.findViewById<Button>(R.id.btnRetry)
                            .setOnClickListener { viewModel.retry() }
                        contentContainer.addView(errView)
                    }
                }
            }
        }
    }

    private fun bindHistory() {
        val hist = searchHistory.getHistory()
        if (hist.isEmpty()) {
            historyRecyclerView.visibility = View.GONE
            return
        }

        // 1) Header
        val headerAdapter = object : RecyclerView.Adapter<TextViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                TextViewHolder(
                    layoutInflater.inflate(R.layout.item_history_header, parent, false)
                )
            override fun getItemCount() = 1
            override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
                holder.textView.text = getString(R.string.history_title)
            }
        }

        // 2) Tracks
        val tracksAdapter = TrackAdapter(hist) { track ->
            onHistoryItemClick(track)
        }

        // 3) Footer
        val footerAdapter = object : RecyclerView.Adapter<ButtonViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ButtonViewHolder(
                    layoutInflater.inflate(R.layout.item_history_footer, parent, false)
                ).apply {
                    button.setOnClickListener {
                        searchHistory.clearHistory()
                        bindHistory()
                    }
                }
            override fun getItemCount() = 1
            override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {}
        }

        historyRecyclerView.adapter = ConcatAdapter(
            headerAdapter,
            tracksAdapter,
            footerAdapter
        )
        historyRecyclerView.visibility = View.VISIBLE

        recyclerView.visibility = View.GONE
        contentContainer.removeAllViews()

        historyRecyclerView.adapter = ConcatAdapter(headerAdapter, tracksAdapter, footerAdapter)
        historyRecyclerView.visibility = View.VISIBLE
        historyRecyclerView.bringToFront()
    }

    private fun setupResultsList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(emptyList()) {}
        recyclerView.adapter = trackAdapter
    }

    private fun setupSearchField() {
        searchEditText.setOnClickListener { showKeyboard() }
        clearButton.setOnClickListener {
            searchEditText.text?.clear()
            hideKeyboard()
            clearButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            if (searchEditText.hasFocus()) bindHistory()
        }
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val empty = s.isNullOrEmpty()
                clearButton.visibility = if (empty) View.GONE else View.VISIBLE

                if (empty && searchEditText.hasFocus()) {
                    bindHistory()
                    recyclerView.visibility = View.GONE
                } else {
                    historyRecyclerView.visibility = View.GONE   // ← прячем историю при любом вводе
                    recyclerView.visibility = View.VISIBLE       // ← показываем список результатов
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnter  = event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                    event.action == android.view.KeyEvent.ACTION_DOWN
            if (isSearch || isEnter) {
                viewModel.search(searchEditText.text.toString())
                hideKeyboard()
                true
            } else false
        }

        /*история исчезает, если фокуса нет или пользователь начал ввод*/
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            val isEmpty = searchEditText.text.isNullOrEmpty()
            if (hasFocus && isEmpty) {
                bindHistory()
            } else {
                historyRecyclerView.visibility = View.GONE
            }
        }


    }

    private fun onHistoryItemClick(track: Track) {
        searchHistory.saveTrack(track)        // переместит в начало
        searchEditText.setText(track.trackName)
        searchEditText.setSelection(searchEditText.text?.length ?: 0)
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

    override fun onResume() {
        super.onResume()
        if (searchEditText.text.isEmpty()) bindHistory()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewHeader)
    }
    private class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.clearHistoryFooter)
    }
}
