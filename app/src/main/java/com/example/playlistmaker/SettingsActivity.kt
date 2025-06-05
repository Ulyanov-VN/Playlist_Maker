package com.example.playlistmaker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.net.toUri

object ThemeHelper {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "is_night_mode"

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isNightMode = prefs.getBoolean(KEY_THEME, false)
        setNightMode(activity, isNightMode)
    }

    fun toggleTheme(activity: Activity, isNightMode: Boolean) {
        setNightMode(activity, isNightMode)
        saveTheme(activity, isNightMode)
    }

    private fun setNightMode(activity: Activity, isNightMode: Boolean) {
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

        // Применяем текущую тему
        ThemeHelper.applyTheme(this)

        // Находим и настраиваем кнопки
        val buttonBack = findViewById<ImageButton>(R.id.icon_button)
        buttonBack.setOnClickListener { finish() }

        val lineShareApp = findViewById<LinearLayout>(R.id.line_share_app)
        lineShareApp.setOnClickListener { shareApp() }

        val lineSupport = findViewById<LinearLayout>(R.id.line_support)
        lineSupport.setOnClickListener { contactSupport() }

        val lineArrow = findViewById<LinearLayout>(R.id.line_arrow)
        lineArrow.setOnClickListener { openTerms() }

        // Настройка переключателя темы
        val themeSwitch = findViewById<SwitchCompat>(R.id.themeSwitch)
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        themeSwitch.isChecked = prefs.getBoolean("is_night_mode", false)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemeHelper.toggleTheme(this, isChecked)
            recreate() // Пересоздаем активити для применения темы
        }
    }

    private fun shareApp() {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text)) // ← Здесь
            startActivity(Intent.createChooser(this, getString(R.string.share_app_title))) // ← И здесь
        }
    }

    private fun contactSupport() {
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email))) // ← Здесь
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject)) // ← Здесь
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body)) // ← Здесь
            startActivity(this)
        }
    }

    private fun openTerms() {
        val termsUrl = getString(R.string.terms_url)
        Log.d("SettingsActivity", "Opening URL: $termsUrl") // Добавьте эту строку
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
        startActivity(intent)
    }
}



