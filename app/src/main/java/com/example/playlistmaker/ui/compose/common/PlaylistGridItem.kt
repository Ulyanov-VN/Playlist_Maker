package com.example.playlistmaker.ui.compose.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.playlist.domain.model.Playlist

private val YsDisplayRegular = FontFamily(Font(R.font.ys_display_regular))

@Composable
fun PlaylistGridItem(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    val textColor = playlistMakerControlText()
    val placeholderRes = if (isSystemInDarkTheme()) {
        R.drawable.placeholder_dark
    } else {
        R.drawable.placeholder_light
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        GlideImage(
            model = playlist.coverImagePath ?: placeholderRes,
            contentDescription = null,
            placeholderResId = placeholderRes,
            cornerRadiusDp = 8,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
        )

        Text(
            text = playlist.name,
            modifier = Modifier.padding(top = 8.dp),
            color = textColor,
            fontFamily = YsDisplayRegular,
            fontWeight = FontWeight.W400,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = pluralStringResource(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount
            ),
            modifier = Modifier.padding(top = 4.dp),
            color = textColor,
            fontFamily = YsDisplayRegular,
            fontWeight = FontWeight.W400,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}