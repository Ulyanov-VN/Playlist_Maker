package com.example.playlistmaker

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*


data class Track(
    @SerializedName("trackId") val trackId: Long,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("collectionName") val collectionName: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("previewUrl") val previewUrl: String?
) : Parcelable {
    val trackTime: String
        get() = trackTimeMillis
            ?.let { SimpleDateFormat("mm:ss", Locale.getDefault()).format(it) }
            ?: "--:--"

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString()
    )


    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }

    // Функция для получения ссылки на обложку 512x512
    fun getCoverArtwork(): String? {
        return artworkUrl100?.replaceAfterLast("/", "512x512bb.jpg")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(trackId)
        parcel.writeString(trackName)
        parcel.writeString(artistName)
        parcel.writeString(collectionName)
        parcel.writeString(releaseDate)
        parcel.writeString(primaryGenreName)
        parcel.writeString(country)
        parcel.writeValue(trackTimeMillis)
        parcel.writeString(artworkUrl100)
        parcel.writeString(previewUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }
}