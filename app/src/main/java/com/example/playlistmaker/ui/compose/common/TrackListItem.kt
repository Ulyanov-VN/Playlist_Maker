package com.example.playlistmaker.ui.compose.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.entity.Track

private val YsTextRegular = FontFamily(Font(R.font.ys_text_regular))

@Composable
fun TrackListItem(
    track: Track,
    formatTrackTime: (Long?) -> String,
    onClick: () -> Unit,
) {
    val titleColor = playlistMakerControlText()
    val subtitleColor = playlistMakerSecondaryText()
    val arrowTint = playlistMakerSecondaryText()
    val placeholderRes = if (isSystemInDarkTheme()) {
        R.drawable.placeholder_dark
    } else {
        R.drawable.placeholder_light
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 13.dp, end = 13.dp, top = 16.dp)
    ) {
        GlideImage(
            model = track.artworkUrl100,
            contentDescription = null,
            placeholderResId = placeholderRes,
            cornerRadiusDp = 8,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.trackName ?: "",
                color = titleColor,
                fontFamily = YsTextRegular,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${track.artistName.orEmpty()} • ${formatTrackTime(track.trackTimeMillis)}",
                color = subtitleColor,
                fontFamily = YsTextRegular,
                fontWeight = FontWeight.W400,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.arrow),
            contentDescription = null,
            tint = arrowTint
        )
    }
}