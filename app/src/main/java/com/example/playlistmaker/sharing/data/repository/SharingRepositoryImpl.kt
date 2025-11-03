package com.example.playlistmaker.sharing.data.repository

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.repository.SharingRepository

class SharingRepositoryImpl(
    private val context: Context
) : SharingRepository {

    override fun getShareAppContent(): String =
        context.getString(R.string.share_app_text)

    override fun getSupportEmailData(): Triple<String, String, String> =
        Triple(
            context.getString(R.string.support_email),
            context.getString(R.string.support_subject),
            context.getString(R.string.support_body)
        )

    override fun getTermsUrl(): String =
        context.getString(R.string.terms_url)
}