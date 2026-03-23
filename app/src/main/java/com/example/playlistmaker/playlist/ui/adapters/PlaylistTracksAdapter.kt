package com.example.playlistmaker.playlist.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.entity.Track
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistTracksAdapter(
    private var tracks: List<Track>,
    private val onClick: (Track) -> Unit,
    private val onLongClick: (Track) -> Unit
) : RecyclerView.Adapter<PlaylistTracksAdapter.TrackViewHolder>() {

    fun submitList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        // ⚠️ Тут укажи имя файла layout элемента трека (скорее всего item_track.xml)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    class TrackViewHolder(
        itemView: View,
        private val onClick: (Track) -> Unit,
        private val onLongClick: (Track) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val artwork: ImageView = itemView.findViewById(R.id.artwork)
        private val trackName: TextView = itemView.findViewById(R.id.trackName)
        private val artistName: TextView = itemView.findViewById(R.id.artistName)
        private val trackTime: TextView = itemView.findViewById(R.id.trackTime)

        fun bind(track: Track) {
            trackName.text = track.trackName.orEmpty()
            artistName.text = track.artistName.orEmpty()

            // время
            val millis = track.trackTimeMillis ?: 0L
            trackTime.text = formatMillisToMmSs(millis)

            // плейсхолдер (можешь сделать как у тебя в PlaylistAdapter по теме)
            val placeholderRes = R.drawable.placeholder_dark

            val url = track.artworkUrl100
            if (url.isNullOrEmpty()) {
                artwork.setImageResource(placeholderRes)
            } else {
                Glide.with(artwork)
                    .load(url)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .centerCrop()
                    .into(artwork)
            }

            itemView.setOnClickListener { onClick(track) }
            itemView.setOnLongClickListener {
                onLongClick(track)
                true
            }
        }

        private fun formatMillisToMmSs(millis: Long): String {
            // 00:00
            return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
        }
    }
}
