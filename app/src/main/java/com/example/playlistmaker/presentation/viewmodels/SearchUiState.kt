package com.example.playlistmaker.presentation.viewmodels

import com.example.playlistmaker.domain.entity.Track

sealed class SearchUiState {
    object Empty : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val tracks: List<Track>) : SearchUiState()
    object NoResults : SearchUiState()
    object Error : SearchUiState()
}