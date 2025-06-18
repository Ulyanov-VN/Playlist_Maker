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

class TrackAdapter(private var tracks: List<Track>) : RecyclerView.Adapter<TrackViewHolder>() {

    private val allTracks: List<Track> = tracks.toList() // Создаем копию оригинального списка

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent) // Теперь ViewHolder сам управляет inflate
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    fun filter(query: String) {
        tracks = if (query.isEmpty()) {
            allTracks
        } else {
            allTracks.filter {
                it.trackName.contains(query, ignoreCase = true) ||
                        it.artistName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}


