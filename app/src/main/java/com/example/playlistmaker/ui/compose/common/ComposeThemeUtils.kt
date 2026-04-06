package com.example.playlistmaker.ui.compose.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun playlistMakerScreenBackground(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFF1A1B22)
    } else {
        Color(0xFFFFFFFF)
    }
}

@Composable
fun playlistMakerPrimaryText(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF1A1B22)
    }
}

@Composable
fun playlistMakerSecondaryText(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFFAEAFB4)
    }
}

@Composable
fun playlistMakerControlText(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF1A1B22)
    }
}