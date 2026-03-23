package com.example.playlistmaker.search.ui.viewmodels

import com.example.playlistmaker.search.domain.entity.Track

sealed class SearchUiState {
    object Empty : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val tracks: List<Track>) : SearchUiState()
    object NoResults : SearchUiState()
    object Error : SearchUiState()
}