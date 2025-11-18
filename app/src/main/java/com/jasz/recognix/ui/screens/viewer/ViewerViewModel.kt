package com.jasz.recognix.ui.screens.viewer

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ViewerViewState(
    val image: ImageEntity? = null
)

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerViewState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val imageUri: Uri = (savedStateHandle.get<String>("uri") ?: "").toUri()

    init {
        viewModelScope.launch {
            val imageEntity = mediaRepository.getImageByUri(imageUri.toString())
            _uiState.value = _uiState.value.copy(image = imageEntity)
        }
    }

    fun onJumpToLocationClicked() {
        viewModelScope.launch {
            _uiState.value.image?.folderPath?.let { path ->
                val albums = mediaRepository.getAlbums().first() // Collect the first emission
                val album = albums.find { path.startsWith(it.path) }
                if (album != null) {
                    _navigationEvent.emit("album/${album.path}/${album.label}")
                }
            }
        }
    }

    fun deleteImage() {
        viewModelScope.launch {
            mediaRepository.deleteImage(imageUri)
            _navigationEvent.emit("-1") // Navigate back
        }
    }

    fun renameImage(newName: String) {
        viewModelScope.launch {
            mediaRepository.renameImage(imageUri, newName)
            // Refresh the image info
            val imageEntity = mediaRepository.getImageByUri(imageUri.toString())
            _uiState.value = _uiState.value.copy(image = imageEntity)
        }
    }
}
