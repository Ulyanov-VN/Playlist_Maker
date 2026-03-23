package com.example.playlistmaker.playlist.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBinding
import com.example.playlistmaker.playlist.domain.model.Playlist

class PlaylistAdapter(
    private var playlists: List<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
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
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            val trackCountText = binding.root.context.resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount
            )
            binding.trackCount.text = trackCountText


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
                    .transform(RoundedCorners(16))
                    .into(binding.coverImage)
            } else {
                Glide.with(binding.root)
                    .load(placeholderResId)
                    .centerCrop()
                    .transform(RoundedCorners(16))
                    .into(binding.coverImage)
            }

            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }


    }
}