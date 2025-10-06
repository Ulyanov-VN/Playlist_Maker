package com.example.playlistmaker.presentation.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.domain.entity.Track
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.interactor.FormatTimeInteractor

class TrackAdapter(
    var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackViewHolder>() {

    private val formatTimeInteractor: FormatTimeInteractor = Creator.provideFormatTimeInteractor()
    private val allTracks: List<Track> = tracks.toList()

    override fun getItemCount(): Int = tracks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track, formatTimeInteractor)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
    }

    fun filter(query: String) {
        val q = query.trim()
        tracks = if (q.isEmpty()) {
            allTracks
        } else {
            allTracks.filter {
                it.trackName.orEmpty().contains(q, ignoreCase = true) ||
                        it.artistName.orEmpty().contains(q, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}