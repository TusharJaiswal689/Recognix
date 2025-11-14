package com.jasz.recognix.ui.screens.album

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val images: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState = _uiState.asStateFlow()

    val albumPath: String = savedStateHandle.get<String>("albumPath") ?: ""

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val images = mediaRepository.getImagesForAlbum(albumPath)
                _uiState.value = _uiState.value.copy(images = images, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun clearIndex() {
        viewModelScope.launch {
            mediaRepository.clearIndexForFolder(albumPath)
        }
    }
}
