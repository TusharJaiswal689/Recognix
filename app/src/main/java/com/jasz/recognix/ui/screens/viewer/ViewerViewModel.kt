package com.jasz.recognix.ui.screens.viewer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ViewerViewState(
    val imageName: String? = null
)

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerViewState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val imageUri: Uri = savedStateHandle.get<String>("imageUri")?.let { Uri.parse(it) }!!

    init {
        _uiState.value = _uiState.value.copy(imageName = imageUri.lastPathSegment)
    }

    fun onJumpToLocationClicked() {
        viewModelScope.launch {
            val imageEntity = mediaRepository.getImageByUri(imageUri.toString())
            if (imageEntity != null) {
                val album = mediaRepository.getAlbums().find { it.path == imageEntity.folderPath }
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
}
