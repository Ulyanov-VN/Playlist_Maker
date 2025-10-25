package com.example.playlistmaker.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.di.Creator
import com.example.playlistmaker.domain.interactor.ManageThemeInteractor
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected var bottomNavigationView: BottomNavigationView? = null
    protected lateinit var manageThemeInteractor: ManageThemeInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        // Инициализация интерактора перед setContentView
        manageThemeInteractor = Creator.provideManageThemeInteractor(this)

        // Применение темы
        applyTheme()

        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        bottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView?.let { bnv ->
            setupBottomNavigation(bnv)
            highlightCurrentMenuItem(bnv)
        }
    }

    abstract fun getLayoutId(): Int

    protected fun applyTheme() {
        val isNightMode = manageThemeInteractor.isDarkThemeEnabled()
        setNightMode(isNightMode)
    }

    protected fun setNightMode(isNightMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    protected fun toggleTheme(): Boolean {
        val currentState = manageThemeInteractor.isDarkThemeEnabled()
        val newState = !currentState

        manageThemeInteractor.setDarkThemeEnabled(newState)
        setNightMode(newState)

        return newState
    }

    private fun setupBottomNavigation(bnv: BottomNavigationView) {
        bnv.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_search -> {
                    if (this !is SearchActivity) {
                        startActivity(Intent(this, SearchActivity::class.java))
                        finish()
                    }
                    true
                }

                R.id.nav_media -> {
                    if (this !is MediaActivity) {
                        startActivity(Intent(this, MediaActivity::class.java))
                        finish()
                    }
                    true
                }

                R.id.nav_settings -> {
                    if (this !is SettingsActivity) {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        finish()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun highlightCurrentMenuItem(bnv: BottomNavigationView) {
        val menu = bnv.menu
        when (this) {
            is SearchActivity -> menu.findItem(R.id.nav_search).isChecked = true
            is MediaActivity -> menu.findItem(R.id.nav_media).isChecked = true
            is SettingsActivity -> menu.findItem(R.id.nav_settings).isChecked = true
        }
    }
}