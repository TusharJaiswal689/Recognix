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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val imageUri: Uri = (savedStateHandle.get<String>("uri") ?: "").toUri()

    val uiState: StateFlow<ViewerViewState> = mediaRepository.getImageByUri(imageUri.toString())
        .map { ViewerViewState(image = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewerViewState(image = null)
        )

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun deleteImage() {
        viewModelScope.launch {
            mediaRepository.deleteImage(imageUri)
            _navigationEvent.emit("-1") // Navigate back
        }
    }

    fun renameImage(newName: String) {
        viewModelScope.launch {
            mediaRepository.renameImage(imageUri, newName)
        }
    }
}

data class ViewerViewState(
    val image: ImageEntity?
)
