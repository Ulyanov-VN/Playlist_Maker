package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.compose.SettingsScreen
import com.example.playlistmaker.settings.ui.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val initialThemeState = viewModel.isDarkThemeEnabled()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SettingsScreen(
                    initialDarkThemeEnabled = initialThemeState,
                    onThemeChanged = { isChecked ->
                        viewModel.setDarkThemeEnabled(isChecked)
                        AppCompatDelegate.setDefaultNightMode(
                            if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                            else AppCompatDelegate.MODE_NIGHT_NO
                        )
                        requireActivity().recreate()
                    },
                    onShareAppClick = { shareApp() },
                    onSupportClick = { contactSupport() },
                    onTermsClick = { openTerms() }
                )
            }
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
}