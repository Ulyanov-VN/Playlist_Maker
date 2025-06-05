package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // Установка изображений и текстов
        tracksImage.setImageResource(R.drawable.smile)
        tracksText.text = getString(R.string.text_favorite_tracks)

        playlistsImage.setImageResource(R.drawable.smile)
        playlistsText.text = getString(R.string.text_playlist)

        // Обработчик переключения вкладок
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
}