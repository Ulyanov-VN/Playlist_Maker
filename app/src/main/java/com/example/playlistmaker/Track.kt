package com.example.playlistmaker

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class Track(
    @SerializedName("trackName")      val trackName: String?,
    @SerializedName("artistName")     val artistName: String?,
    @SerializedName("trackTimeMillis")val trackTimeMillis: Long?,
    @SerializedName("artworkUrl100")  val artworkUrl100: String?
) {
    // Отформатированное время, которое раньше лежало в trackTime
    val trackTime: String
        get() = trackTimeMillis
            ?.let { SimpleDateFormat("mm:ss", Locale.getDefault()).format(it) }
            ?: "--:--"
}