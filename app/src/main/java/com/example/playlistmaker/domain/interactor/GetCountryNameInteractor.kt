package com.example.playlistmaker.domain.interactor

import java.util.Locale

interface GetCountryNameInteractor {
    fun execute(countryCode: String?): String
}

class GetCountryNameInteractorImpl : GetCountryNameInteractor {
    override fun execute(countryCode: String?): String {
        return countryCode?.let { code ->
            try {
                val locale = Locale("", code)
                locale.getDisplayCountry(Locale.getDefault()).takeIf { it.isNotBlank() } ?: code
            } catch (e: Exception) {
                code
            }
        } ?: "Unknown"
    }
}