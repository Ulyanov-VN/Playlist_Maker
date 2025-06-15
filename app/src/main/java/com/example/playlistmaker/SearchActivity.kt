package com.example.playlistmaker

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
    private var currentSearchText = "" // Переменная для хранения текста

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
        trackAdapter = TrackAdapter(tracks)
        recyclerView.adapter = trackAdapter
    }



/*    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (isKeyboardVisible) {
                findViewById<BottomNavigationView>(R.id.bottomNav).hide()
            } else {
                findViewById<BottomNavigationView>(R.id.bottomNav).show()
            }
            insets
        }
    }*/

    /*    private fun setupKeyboardListener() {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

            window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
                val rect = Rect()
                window.decorView.getWindowVisibleDisplayFrame(rect)

                val screenHeight = window.decorView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) { // Клавиатура открыта
                    bottomNav.hide()
                } else { // Клавиатура закрыта
                    bottomNav.show()
                }
            }
        }*/


    /*  Автоматический показ клавиатуры при получении фокуса.

        override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && searchEditText.text.isNullOrEmpty()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            searchEditText.requestFocus()
        }
    }*/


    private fun setupSearchField() {
        // 1. Автоматический фокус и показ клавиатуры
        searchEditText.setOnClickListener {
            showKeyboard()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s.toString()
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                s?.takeIf { it.isNotEmpty() }?.let { performSearch(it.toString()) }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.text.toString())
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        // 3. Отслеживание текста
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                s?.takeIf { it.isNotEmpty() }?.let { performSearch(it.toString()) }
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

    /*    private fun showKeyboard() {
        searchEditText.postDelayed({
            searchEditText.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }*/

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(window, searchEditText).hide(
            WindowInsetsCompat.Type.ime()
        )
    }
    /*    private fun hideKeyboard() {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }*/

    private fun performSearch(query: String) {
        Log.d("SearchActivity", "Performing search: $query") // Реализация поиска
        trackAdapter.filter(query) // Фильтруем треки по запросу

    }

    // Сохранение состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_TEXT", currentSearchText)
    }

    // Восстановление состояния
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentSearchText = savedInstanceState.getString("SEARCH_TEXT", "")
        searchEditText.setText(currentSearchText)
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /*    override fun onResume() {
            super.onResume()
            if (searchEditText.text.isNullOrEmpty()) {
                showKeyboard()
            }*/

    val tracks = ArrayList<Track>().apply {
        add(Track("Smells Like Teen Spirit", "Nirvana", "5:01", "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"))
        add(Track("Billie Jean", "Michael Jackson", "4:35", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"))
        add(Track("Stayin' Alive", "Bee Gees", "4:10", "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"))
        add(Track("Whole Lotta Love", "Led Zeppelin", "5:33", "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"))
        add(Track("Sweet Child O'Mine", "Guns N' Roses", "5:03", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"))
    }
}

