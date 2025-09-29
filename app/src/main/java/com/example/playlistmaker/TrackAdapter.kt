package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.ItemTrackBinding

class TrackAdapter(
    var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackViewHolder>() {

    // Копия полного списка для фильтрации
    private val allTracks: List<Track> = tracks.toList()

    override fun getItemCount(): Int = tracks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
    }

    /**
     * Фильтрация по строке запроса.
     * Если query пустой — показываем всё, иначе только совпадения.
     */
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
