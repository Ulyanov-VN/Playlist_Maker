package com.example.playlistmaker.search.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractor

class SearchViewModelFactory(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val manageSearchHistoryInteractor: ManageSearchHistoryInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(searchTracksInteractor, manageSearchHistoryInteractor) as T
    }
}