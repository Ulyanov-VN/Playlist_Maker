package com.example.playlistmaker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

object ThemeHelper {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "is_night_mode"

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isNightMode = prefs.getBoolean(KEY_THEME, false)
        setNightMode(isNightMode)
    }

    fun toggleTheme(activity: Activity, isNightMode: Boolean) {
        setNightMode(isNightMode)
        saveTheme(activity, isNightMode)
    }

    private fun setNightMode(isNightMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun saveTheme(activity: Activity, isNightMode: Boolean) {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_THEME, isNightMode)
            .apply()
    }
}

class SettingsActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Применяем текущую тему (если нужно, перенеси в BaseActivity перед setContentView)
        ThemeHelper.applyTheme(this)

        // Кнопка "назад"
        val buttonBack = findViewById<ImageButton>(R.id.icon_button)
        buttonBack.setOnClickListener { finish() }

        // Пункты меню
        val lineShareApp = findViewById<LinearLayout>(R.id.line_share_app)
        lineShareApp.setOnClickListener { shareApp() }

        val lineSupport = findViewById<LinearLayout>(R.id.line_support)
        lineSupport.setOnClickListener { contactSupport() }

        val lineArrow = findViewById<LinearLayout>(R.id.line_arrow)
        lineArrow.setOnClickListener { openTerms() }

        // Переключатель темы + программная подсветка синим
        val themeSwitch = findViewById<SwitchCompat>(R.id.themeSwitch)
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        themeSwitch.isChecked = prefs.getBoolean("is_night_mode", false)

        tintBlue(themeSwitch) // ← подсветка по состоянию checked

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemeHelper.toggleTheme(this, isChecked)
            recreate() // Пересоздаем активити для применения темы
        }
    }

    private fun shareApp() {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
            startActivity(Intent.createChooser(this, getString(R.string.share_app_title)))
        }
    }

    private fun contactSupport() {
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            startActivity(this)
        }
    }

    private fun openTerms() {
        val termsUrl = getString(R.string.terms_url)
        Log.d("SettingsActivity", "Opening URL: $termsUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
        startActivity(intent)
    }

    /** Тонируем SwitchCompat: синий во включенном состоянии */
    private fun tintBlue(s: SwitchCompat) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),     // ВКЛ
            intArrayOf(-android.R.attr.state_checked)     // ВЫКЛ
        )
        val thumbColors = intArrayOf(
            Color.parseColor("#3772E7"), // кружок: синий (on)
            Color.parseColor("#AEAFB4")  // кружок: серый (off)
        )
        val trackColors = intArrayOf(
            Color.parseColor("#9FBBF3"), // дорожка: голубой (on)
            Color.parseColor("#E6E8EB")  // дорожка: светло-серый (off)
        )
        s.thumbTintList = ColorStateList(states, thumbColors)
        s.trackTintList = ColorStateList(states, trackColors)
    }
}


