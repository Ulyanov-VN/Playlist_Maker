package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor

class SearchViewModelFactory(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val manageSearchHistoryInteractor: ManageSearchHistoryInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(searchTracksInteractor, manageSearchHistoryInteractor) as T
    }
}