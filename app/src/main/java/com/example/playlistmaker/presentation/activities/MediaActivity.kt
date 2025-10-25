package com.example.playlistmaker.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout

class MediaActivity : BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_media

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val tracksTab = findViewById<ConstraintLayout>(R.id.tracksTab)
        val playlistsTab = findViewById<ConstraintLayout>(R.id.playlistsTab)

        val tracksImage = tracksTab.findViewById<ImageView>(R.id.iconSmile)
        val tracksText = tracksTab.findViewById<TextView>(R.id.tracksEmptyText)

        val playlistsImage = playlistsTab.findViewById<ImageView>(R.id.iconSmile2)
        val playlistsText = playlistsTab.findViewById<TextView>(R.id.playlistsEmptyText)

        val isNightTheme =
            when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
                android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }

        val smileIcon = if (isNightTheme) R.drawable.smile_night else R.drawable.smile

        tracksImage.setImageResource(smileIcon)
        playlistsImage.setImageResource(smileIcon)

        tracksText.text = getString(R.string.text_favorite_tracks)
        playlistsText.text = getString(R.string.text_playlist)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        tracksTab.visibility = View.VISIBLE
                        playlistsTab.visibility = View.GONE
                    }
                    1 -> {
                        tracksTab.visibility = View.GONE
                        playlistsTab.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        updateThemeDependentResources()
    }

    private fun updateThemeDependentResources() {
        val isNightTheme =
            when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
                android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }

        val tracksTab = findViewById<ConstraintLayout>(R.id.tracksTab)
        val playlistsTab = findViewById<ConstraintLayout>(R.id.playlistsTab)

        val tracksImage = tracksTab.findViewById<ImageView>(R.id.iconSmile)
        val playlistsImage = playlistsTab.findViewById<ImageView>(R.id.iconSmile2)

        val smileIcon = if (isNightTheme) R.drawable.smile_night else R.drawable.smile
        tracksImage.setImageResource(smileIcon)
        playlistsImage.setImageResource(smileIcon)
    }
}