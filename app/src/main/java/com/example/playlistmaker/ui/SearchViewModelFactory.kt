package com.example.playlistmaker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.repository.SearchRepository
import com.example.playlistmaker.network.RetrofitInstance

class SearchViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val repo = SearchRepository(RetrofitInstance.api)
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}