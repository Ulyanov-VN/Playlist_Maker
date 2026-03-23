package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.activity_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lineShareApp = view.findViewById<LinearLayout>(R.id.line_share_app)
        lineShareApp.setOnClickListener { shareApp() }

        val lineSupport = view.findViewById<LinearLayout>(R.id.line_support)
        lineSupport.setOnClickListener { contactSupport() }

        val lineArrow = view.findViewById<LinearLayout>(R.id.line_arrow)
        lineArrow.setOnClickListener { openTerms() }

        val themeSwitch = view.findViewById<SwitchCompat>(R.id.themeSwitch)
        themeSwitch.isChecked = viewModel.isDarkThemeEnabled()

        tintBlue(themeSwitch)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkThemeEnabled(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            requireActivity().recreate()
        }
    }

    private fun shareApp() {
        val shareText = viewModel.getShareAppContent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_app_title)))
    }

    private fun contactSupport() {
        val (email, subject, body) = viewModel.getSupportEmailData()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(intent)
    }

    private fun openTerms() {
        val termsUrl = viewModel.getTermsUrl()
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
