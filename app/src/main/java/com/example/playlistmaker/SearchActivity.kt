package com.example.playlistmaker

import TrackAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.ui.search.SearchUiState
import com.example.playlistmaker.ui.search.SearchViewModel
import com.example.playlistmaker.ui.search.SearchViewModelFactory
import kotlinx.coroutines.launch


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

        val buttonBack = findViewById<ImageButton>(R.id.icon_button)
        buttonBack.setOnClickListener { finish() }

        // Инициализация элементов
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)

        setupSearchField()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(emptyList())
        recyclerView.adapter = trackAdapter

        contentContainer = findViewById(R.id.mainContent)
        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            val params = FrameLayout.LayoutParams(360, 640).apply {
                gravity = Gravity.CENTER
            }
            layoutParams = params
        }

        // Изменение состояния
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Скрываем всегда XML-RecyclerView и очищаем контейнер
                recyclerView.visibility = View.GONE
                contentContainer.removeAllViews()



                when (state) {
                    is SearchUiState.Empty -> {
                        // Ничего не отображается
                    }

                    is SearchUiState.Loading -> {
                        // Отображение ProgressBar
                        contentContainer.addView(progressBar)
                    }

                    is SearchUiState.Success -> {
                        // RecyclerView сделали видимым
                        recyclerView.visibility = View.VISIBLE

                        // Обновление данных адаптера
                        trackAdapter = TrackAdapter(state.tracks)
                        recyclerView.adapter = trackAdapter

                        // Фильтр по введённому тексту
                        val searchText = searchEditText.text.toString()
                        trackAdapter.filter(searchText)
                    }

                    is SearchUiState.NoResults -> {
                        // Картинка «пусто»
                        val emptyView = layoutInflater.inflate(
                            R.layout.placeholder_empty,
                            contentContainer,
                            false
                        )
                        contentContainer.addView(emptyView)
                    }

                    is SearchUiState.Error -> {
                        // Плейсхолдер с «Обновить»
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
        // Автоматический фокус и показ клавиатуры
        searchEditText.setOnClickListener {
            showKeyboard()
        }

        // Обработчики взаимодействий
        clearButton.setOnClickListener {
            searchEditText.text?.clear()
            hideKeyboard()
            clearButton.visibility = View.GONE

            // Установка пустого списка треков в адаптер
            trackAdapter = TrackAdapter(emptyList())
            recyclerView.adapter = trackAdapter
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

