package com.example.playlistmaker.search.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemTrackBinding
import com.example.playlistmaker.player.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.search.domain.entity.Track

class TrackViewHolder(
    parent: ViewGroup,
    private val binding: ItemTrackBinding = ItemTrackBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    )
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track, formatTimeInteractor: FormatTimeInteractor) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackTime.text = formatTimeInteractor.executeForTrack(track.trackTimeMillis)

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
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .centerCrop()
                .transform(RoundedCorners(16))
                .into(artwork)
        }
    }
}