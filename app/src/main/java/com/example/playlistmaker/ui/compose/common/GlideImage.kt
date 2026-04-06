package com.example.playlistmaker.ui.compose.common

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

@Composable
fun GlideImage(
    model: Any?,
    contentDescription: String?,
    placeholderResId: Int,
    cornerRadiusDp: Int,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                this.contentDescription = contentDescription
            }
        },
        update = { imageView ->
            Glide.with(imageView)
                .load(model)
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .centerCrop()
                .transform(
                    RoundedCorners(
                        (cornerRadiusDp * imageView.resources.displayMetrics.density).toInt()
                    )
                )
                .into(imageView)
        }
    )
}