package com.example.playlistmaker.settings.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.compose.common.playlistMakerPrimaryText
import com.example.playlistmaker.ui.compose.common.playlistMakerScreenBackground
import com.example.playlistmaker.ui.compose.common.playlistMakerSecondaryText

private val YsDisplayMedium = FontFamily(Font(R.font.ys_display_medium))
private val YsDisplayRegular = FontFamily(Font(R.font.ys_display_regular))

@Composable
fun SettingsScreen(
    initialDarkThemeEnabled: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onShareAppClick: () -> Unit,
    onSupportClick: () -> Unit,
    onTermsClick: () -> Unit,
) {
    var darkThemeEnabled by rememberSaveable { mutableStateOf(initialDarkThemeEnabled) }

    val backgroundColor = playlistMakerScreenBackground()
    val textColor = playlistMakerPrimaryText()
    val iconTint = playlistMakerSecondaryText()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.settings),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 16.dp),
            textAlign = TextAlign.Start,
            color = textColor,
            fontFamily = YsDisplayMedium,
            fontWeight = FontWeight.W500,
            fontSize = 22.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            DarkThemeRow(
                checked = darkThemeEnabled,
                textColor = textColor,
                onCheckedChange = { checked ->
                    darkThemeEnabled = checked
                    onThemeChanged(checked)
                }
            )

            SettingsActionRow(
                text = stringResource(R.string.share_app),
                iconRes = R.drawable.share,
                textColor = textColor,
                iconTint = iconTint,
                onClick = onShareAppClick
            )

            SettingsActionRow(
                text = stringResource(R.string.wr_support),
                iconRes = R.drawable.support,
                textColor = textColor,
                iconTint = iconTint,
                onClick = onSupportClick
            )

            SettingsActionRow(
                text = stringResource(R.string.user_agreement),
                iconRes = R.drawable.arrow,
                textColor = textColor,
                iconTint = iconTint,
                onClick = onTermsClick
            )
        }
    }
}

@Composable
private fun DarkThemeRow(
    checked: Boolean,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 16.dp, end = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dark_theme),
                color = textColor,
                fontFamily = YsDisplayRegular,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(0.82f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF3772E7),
                    checkedTrackColor = Color(0xFF9FBBF3),
                    uncheckedThumbColor = Color(0xFFAEAFB4),
                    uncheckedTrackColor = Color(0xFFE6E8EB),
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    text: String,
    iconRes: Int,
    textColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                fontFamily = YsDisplayRegular,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp
            )

            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}