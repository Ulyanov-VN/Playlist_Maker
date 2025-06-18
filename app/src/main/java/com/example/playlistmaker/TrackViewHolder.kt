package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ItemTrackBinding

class TrackViewHolder(
    parent: ViewGroup,
    private val binding: ItemTrackBinding = ItemTrackBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    )
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime.text = track.trackTime
            val isDarkTheme =
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            val placeholderResId = if (isDarkTheme) {
                R.drawable.placeholder_dark
            } else {
                R.drawable.placeholder_light
            }
            Glide.with(itemView)
                .load(track.artworkUrl100)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(placeholderResId)
                .centerCrop()
                .transform(RoundedCorners(16))
                .into(artwork)
        }
    }
}