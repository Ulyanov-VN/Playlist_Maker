package com.example.playlistmaker.sharing.domain.repository

interface SharingRepository {
    fun getShareAppContent(): String
    fun getSupportEmailData(): Triple<String, String, String>
    fun getTermsUrl(): String
}