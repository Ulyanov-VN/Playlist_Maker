import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.Track
import com.example.playlistmaker.TrackViewHolder

class TrackAdapter(var tracks: List<Track>) : RecyclerView.Adapter<TrackViewHolder>() {
    // Создаём копию для полного списка (allTracks)
    private val allTracks: List<Track> = tracks.toList()

    override fun getItemCount(): Int = tracks.size
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    // Новая реализация:
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
}

/*fun filter(query: String) {
    tracks = if (query.isEmpty()) {
        allTracks
    } else {
        allTracks.filter {
            it.trackName.orEmpty().contains(query, ignoreCase = true) ||
                    it.artistName.orEmpty().contains(query, ignoreCase = true)
        }
    }
    notifyDataSetChanged()
}*/