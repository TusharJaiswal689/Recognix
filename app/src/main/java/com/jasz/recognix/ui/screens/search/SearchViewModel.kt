package com.jasz.recognix.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<ImageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(private val mediaRepository: MediaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchImages(currentFolder: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            mediaRepository.searchImages(_uiState.value.query, currentFolder)
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message, isLoading = false) }
                .collect { results ->
                    _uiState.value = _uiState.value.copy(results = results, isLoading = false)
                }
        }
    }

    fun clearSearch() {
        _uiState.value = SearchUiState()
        searchJob?.cancel()
    }
}
