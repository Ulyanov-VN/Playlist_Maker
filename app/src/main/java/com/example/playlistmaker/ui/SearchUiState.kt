package com.example.playlistmaker.ui.search

sealed class SearchUiState {
    object Empty      : SearchUiState()
    object Loading    : SearchUiState()
    data class Success(val tracks: List<com.example.playlistmaker.Track>) : SearchUiState()
    object NoResults  : SearchUiState()
    object Error      : SearchUiState()
}