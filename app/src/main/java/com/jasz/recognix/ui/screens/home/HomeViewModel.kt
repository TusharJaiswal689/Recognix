package com.jasz.recognix.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.local.datastore.PreferenceDataStore
import com.jasz.recognix.data.model.Album
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val unfinishedScanPath: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val preferenceDataStore: PreferenceDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val unfinishedScanPath = preferenceDataStore.scanFolderPath.first()
            _uiState.value = _uiState.value.copy(unfinishedScanPath = unfinishedScanPath)
        }
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val albums = mediaRepository.getAlbums()
                _uiState.value = _uiState.value.copy(albums = albums, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun clearUnfinishedScan() {
        viewModelScope.launch {
            preferenceDataStore.setScanFolderPath(null)
            _uiState.value = _uiState.value.copy(unfinishedScanPath = null)
        }
    }

    fun clearIndex(folderPath: String) {
        viewModelScope.launch {
            mediaRepository.clearIndexForFolder(folderPath)
        }
    }
}
