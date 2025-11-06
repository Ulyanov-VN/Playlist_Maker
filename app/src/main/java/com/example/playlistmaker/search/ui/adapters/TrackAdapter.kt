package com.example.playlistmaker.search.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.player.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.search.domain.entity.Track

class TrackAdapter(
    var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackViewHolder>() {

    private val formatTimeInteractor: FormatTimeInteractor = Creator.provideFormatTimeInteractor()

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

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}