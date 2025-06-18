package com.example.playlistmaker

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.playlistmaker.data.repository.SearchRepository
import com.example.playlistmaker.network.RetrofitInstance
import com.example.playlistmaker.ui.search.SearchViewModel
import com.example.playlistmaker.ui.search.SearchViewModelFactory
import com.example.playlistmaker.ui.search.SearchUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Button
import com.example.playlistmaker.R
import TrackAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.hide() {
    if (visibility == View.VISIBLE) {
        animate()
            .translationY(height.toFloat())
            .setDuration(200)
            .withEndAction { visibility = View.GONE }
            .start()
    }
}

fun BottomNavigationView.show() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
        animate()
            .translationY(0f)
            .setDuration(200)
            .start()
    }
}

class SearchActivity : BaseActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private val viewModel: SearchViewModel by viewModels { SearchViewModelFactory() }
    private lateinit var contentContainer: FrameLayout
    private lateinit var progressBar: ProgressBar

    override fun getLayoutId(): Int = R.layout.activity_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(R.layout.activity_search)

        /* window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)*/
        val buttonBack = findViewById<ImageButton>(R.id.icon_button)
        buttonBack.setOnClickListener { finish() }

        // Инициализация элементов
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)

       /* setupKeyboardListener()*/
        setupSearchField()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(emptyList())
        recyclerView.adapter = trackAdapter

        contentContainer = findViewById(R.id.mainContent)
        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

// Подписка на изменения состояния
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // 1) Скрываем всегда XML-RecyclerView и очищаем контейнер
                recyclerView.visibility = View.GONE
                contentContainer.removeAllViews()

                when (state) {
                    is SearchUiState.Empty -> {
                        // Ничего не показываем
                    }
                    is SearchUiState.Loading -> {
                        // Показываем только ProgressBar
                        contentContainer.addView(progressBar)
                    }
                    is SearchUiState.Success -> {
                        // 2) Делаем RecyclerView видимым
                        recyclerView.visibility = View.VISIBLE

                        // Обновляем данные адаптера
                        trackAdapter = TrackAdapter(state.tracks)
                        recyclerView.adapter = trackAdapter

                        // Если хотите фильтровать по введённому тексту:
                        val searchText = searchEditText.text.toString()
                        trackAdapter.filter(searchText)
                    }
                    is SearchUiState.NoResults -> {
                        // Показываем картинку «пусто»
                        val emptyView = layoutInflater.inflate(
                            R.layout.placeholder_empty,
                            contentContainer,
                            false
                        )
                        contentContainer.addView(emptyView)
                    }
                    is SearchUiState.Error -> {
                        // 3) Показываем плейсхолдер с «Обновить»
                        val errView = layoutInflater.inflate(
                            R.layout.placeholder_error,
                            contentContainer,
                            false
                        )
                        errView.findViewById<Button>(R.id.btnRetry)
                            .setOnClickListener { viewModel.retry() }
                        contentContainer.addView(errView)
                    }
                }
            }
        }
    }

    private fun setupSearchField() {
        // 1. Автоматический фокус и показ клавиатуры
        searchEditText.setOnClickListener {
            showKeyboard()
        }
         // 2. Обработчики взаимодействий
        clearButton.setOnClickListener {
            searchEditText.text?.clear()
            hideKeyboard()
            clearButton.visibility = View.GONE
            trackAdapter.filter("")
        }

        searchEditText.setOnClickListener {
            showKeyboard()
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchEditText.text.toString())
                hideKeyboard()
                true
            } else false
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Показываем крестик, но не запускаем поиски на каждый ввод
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

    }

    // Методы управления клавиатурой
    private fun showKeyboard() {
        searchEditText.post {
            searchEditText.requestFocus()
            WindowCompat.getInsetsController(window, searchEditText).show(
                WindowInsetsCompat.Type.ime()
            )
        }
    }

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(window, searchEditText).hide(
            WindowInsetsCompat.Type.ime()
        )
    }

    private fun performSearch(query: String) {
        viewModel.search(query)
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}

