package com.example.playlistmaker.playlist.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBottomSheetBinding
import com.example.playlistmaker.playlist.domain.model.Playlist

class PlaylistBottomSheetAdapter(
    private var playlists: List<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistViewHolder>() {

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBottomSheetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            binding.trackCount.text = formatTrackCount(playlist.trackCount)

            // Загрузка обложки
            val isDarkTheme = binding.root.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
            val placeholderResId = if (isDarkTheme) R.drawable.placeholder_dark else R.drawable.placeholder_light

            if (playlist.coverImagePath != null) {
                Glide.with(binding.root)
                    .load(playlist.coverImagePath)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .centerCrop()
                    .transform(RoundedCorners(8))
                    .into(binding.coverImage)
            } else {
                Glide.with(binding.root)
                    .load(placeholderResId)
                    .centerCrop()
                    .transform(RoundedCorners(8))
                    .into(binding.coverImage)
            }

            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }

        private fun formatTrackCount(count: Int): String {
            return when {
                count == 0 -> binding.root.context.getString(R.string.no_tracks)
                count % 10 == 1 && count % 100 != 11 -> "$count трек"
                count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
                else -> "$count треков"
            }
        }
    }
}