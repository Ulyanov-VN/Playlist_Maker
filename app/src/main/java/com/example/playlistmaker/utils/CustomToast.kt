package com.example.playlistmaker.utils

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.playlistmaker.R

object CustomToast {

    enum class ToastType {
        SUCCESS, // Трек добавлен в плейлист
        INFO,    // Плейлист создан
        WARNING  // Трек уже есть в плейлисте
    }

    fun show(
        context: android.content.Context,
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Int = Toast.LENGTH_SHORT,
        position: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        yOffset: Int = 100
    ): Toast {
        // Inflate кастомный layout
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)


        val textView = layout.findViewById<android.widget.TextView>(R.id.toast_text)


        // Устанавливаем текст
        textView.text = message

        // Создаем и настраиваем Toast
        val toast = Toast(context)
        toast.duration = duration
        toast.view = layout

        // Позиционируем Toast
        toast.setGravity(position, 0, yOffset)

        // Настраиваем анимацию (опционально)
        // toast.view?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.toast_slide_up))

        toast.show()
        return toast
    }

    // Удобные методы для конкретных сценариев
    fun showTrackAddedToPlaylist(
        context: android.content.Context,
        playlistName: String,
        duration: Int = Toast.LENGTH_SHORT
    ): Toast {
        return show(
            context = context,
            message = context.getString(R.string.added_to_playlist, playlistName),
            type = ToastType.SUCCESS,
            duration = duration
        )
    }

    fun showPlaylistCreated(
        context: android.content.Context,
        playlistName: String,
        duration: Int = Toast.LENGTH_LONG
    ): Toast {
        return show(
            context = context,
            message = context.getString(R.string.playlist_created, playlistName),
            type = ToastType.INFO,
            duration = duration
        )
    }

    fun showTrackAlreadyInPlaylist(
        context: android.content.Context,
        playlistName: String,
        duration: Int = Toast.LENGTH_SHORT
    ): Toast {
        return show(
            context = context,
            message = context.getString(R.string.track_already_in_playlist, playlistName),
            type = ToastType.WARNING,
            duration = duration
        )
    }
}