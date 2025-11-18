package com.jasz.recognix.ui.screens.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.model.MediaItem
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val media: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumPath: String = savedStateHandle.get<String>("albumPath") ?: ""

    val uiState: StateFlow<AlbumUiState> = mediaRepository.getMediaForAlbum(albumPath)
        .map { media -> AlbumUiState(media = media) }
        .catch { e -> emit(AlbumUiState(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlbumUiState(isLoading = true)
        )

    fun clearIndex() {
        viewModelScope.launch {
            mediaRepository.clearIndexForFolder(albumPath)
        }
    }
}
