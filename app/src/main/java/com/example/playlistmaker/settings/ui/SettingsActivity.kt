package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.widget.SwitchCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.main.ui.BaseActivity
import com.example.playlistmaker.settings.ui.viewmodels.SettingsViewModel
import com.example.playlistmaker.settings.ui.viewmodels.SettingsViewModelFactory
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SettingsActivity : BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_settings

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(Creator.provideManageThemeInteractor(this))
    }

    private val sharingInteractor: SharingInteractor = Creator.provideSharingInteractor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val buttonBack = findViewById<ImageButton>(R.id.icon_button)
        buttonBack.setOnClickListener { finish() }

        val lineShareApp = findViewById<LinearLayout>(R.id.line_share_app)
        lineShareApp.setOnClickListener { shareApp() }

        val lineSupport = findViewById<LinearLayout>(R.id.line_support)
        lineSupport.setOnClickListener { contactSupport() }

        val lineArrow = findViewById<LinearLayout>(R.id.line_arrow)
        lineArrow.setOnClickListener { openTerms() }

        val themeSwitch = findViewById<SwitchCompat>(R.id.themeSwitch)
        themeSwitch.isChecked = viewModel.isDarkThemeEnabled()

        tintBlue(themeSwitch)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkThemeEnabled(isChecked)
            setNightMode(isChecked)
            recreate()
        }
    }

    private fun shareApp() {
        val shareText = sharingInteractor.getShareAppContent()
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(this, getString(R.string.share_app_title)))
        }
    }

    private fun contactSupport() {
        val (email, subject, body) = sharingInteractor.getSupportEmailData()
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            startActivity(this)
        }
    }

    private fun openTerms() {
        val termsUrl = sharingInteractor.getTermsUrl()
        Log.d("SettingsActivity", "Opening URL: $termsUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
        startActivity(intent)
    }

    private fun tintBlue(s: SwitchCompat) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val thumbColors = intArrayOf(
            Color.parseColor("#3772E7"),
            Color.parseColor("#AEAFB4")
        )
        val trackColors = intArrayOf(
            Color.parseColor("#9FBBF3"),
            Color.parseColor("#E6E8EB")
        )
        s.thumbTintList = ColorStateList(states, thumbColors)
        s.trackTintList = ColorStateList(states, trackColors)
    }
}