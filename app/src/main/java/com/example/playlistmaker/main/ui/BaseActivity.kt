package com.example.playlistmaker.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.MediaActivity
import com.example.playlistmaker.search.ui.SearchActivity
import com.example.playlistmaker.settings.ui.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // applyTheme() теперь не нужен — тема выставляется в Application
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        bottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView?.let { bnv ->
            setupBottomNavigation(bnv)
            highlightCurrentMenuItem(bnv)
        }
    }

    abstract fun getLayoutId(): Int

    protected fun setNightMode(isNightMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
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