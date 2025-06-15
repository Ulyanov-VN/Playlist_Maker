package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.view.ViewGroup
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

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder) // Добавляем обработку ошибок
                .centerCrop()
                .transform(RoundedCorners(16))
                .into(artwork)
        }
    }
}
