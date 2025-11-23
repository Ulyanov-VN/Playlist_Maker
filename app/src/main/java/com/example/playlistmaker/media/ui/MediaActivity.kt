package com.example.playlistmaker.media.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.main.ui.BaseActivity
import com.example.playlistmaker.media.ui.adapters.MediaPagerAdapter
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.search.ui.SearchActivity
import com.example.playlistmaker.settings.ui.SettingsActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_media

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var backButton: ImageButton
    private lateinit var title: TextView

    // ВНЕДРЯЕМ ViewModel ЧЕРЕЗ KOIN
    private val favoriteTracksViewModel: FavoriteTracksViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        setupViewPager()
        setupBackButton()
        setupBackPressHandler()


    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        backButton = findViewById(R.id.backButton)
        title = findViewById(R.id.title)

        title.text = getString(R.string.media)
    }

    private fun setupViewPager() {
        val pagerAdapter = MediaPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorite_tracks)
                1 -> getString(R.string.Playlists)
                else -> ""
            }
        }.attach()
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateThemeDependentResources()
        // Подсвечиваем текущий пункт меню в нижней навигации
        highlightCurrentMenuItem()
    }

    private fun updateThemeDependentResources() {
        val isNightTheme =
            when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
                android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }

        val smileIcon = if (isNightTheme) R.drawable.smile_night else R.drawable.smile
        // Обновление иконок будет в соответствующих фрагментах
    }

    private fun highlightCurrentMenuItem() {
        bottomNavigationView?.let { bnv ->
            bnv.menu.findItem(R.id.nav_media).isChecked = true
        }
    }
}