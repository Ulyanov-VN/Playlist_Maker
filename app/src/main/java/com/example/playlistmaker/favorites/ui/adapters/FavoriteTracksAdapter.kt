package com.example.playlistmaker.favorites.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemTrackBinding
import com.example.playlistmaker.search.domain.entity.Track

class FavoriteTracksAdapter(
    private var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<FavoriteTracksAdapter.FavoriteTrackViewHolder>() {

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteTrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteTrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteTrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    inner class FavoriteTrackViewHolder(
        private val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            binding.trackName.text = track.trackName ?: binding.root.context.getString(R.string.unknown_track)
            binding.artistName.text = track.artistName ?: binding.root.context.getString(R.string.unknown_artist)
            binding.trackTime.text = formatTime(track.trackTimeMillis)

            // Загрузка обложки
            val isDarkTheme = binding.root.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
            val placeholderResId = if (isDarkTheme) R.drawable.placeholder_dark else R.drawable.placeholder_light

            Glide.with(binding.root)
                .load(track.artworkUrl100)
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .centerCrop()
                .transform(RoundedCorners(16))
                .into(binding.artwork)

            binding.root.setOnClickListener {
                onTrackClick(track)
            }
        }

        private fun formatTime(millis: Long?): String {
            return if (millis != null) {
                val totalSeconds = millis / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                String.format("%02d:%02d", minutes, seconds)
            } else {
                "--:--"
            }
        }
    }
}