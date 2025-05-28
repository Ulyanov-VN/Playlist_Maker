package com.example.playlistmaker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected var bottomNavigationView: BottomNavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())


        bottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView?.let { bnv ->
            setupBottomNavigation(bnv)
            highlightCurrentMenuItem(bnv)
        }
    }


    abstract fun getLayoutId(): Int

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