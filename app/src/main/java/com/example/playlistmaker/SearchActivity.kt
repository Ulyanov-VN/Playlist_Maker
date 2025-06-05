package com.example.playlistmaker

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
    private var currentSearchText = "" // Переменная для хранения текста

    override fun getLayoutId(): Int = R.layout.activity_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        /* window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)*/
        val buttonBack = findViewById<ImageButton>(R.id.icon_button)
        buttonBack.setOnClickListener { finish() }


        // Инициализация элементов
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)


        setupKeyboardListener()
        setupSearchField()
    }

    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (isKeyboardVisible) {
                findViewById<BottomNavigationView>(R.id.bottomNav).hide()
            } else {
                findViewById<BottomNavigationView>(R.id.bottomNav).show()
            }
            insets
        }
    }

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
        Log.d("SearchActivity", "Performing search: $query")
        // Реализация поиска
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

}

