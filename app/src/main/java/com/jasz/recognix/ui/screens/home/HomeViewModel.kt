package com.jasz.recognix.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jasz.recognix.data.local.datastore.PreferenceDataStore
import com.jasz.recognix.data.model.Album
import com.jasz.recognix.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val albumsFlow = mediaRepository.getAlbums()
    private val unfinishedScanPathFlow = preferenceDataStore.scanFolderPath

    val uiState: StateFlow<HomeUiState> = combine(
        albumsFlow,
        unfinishedScanPathFlow
    ) { albums, unfinishedScanPath ->
        HomeUiState(albums = albums, unfinishedScanPath = unfinishedScanPath)
    }
    .catch { e -> emit(HomeUiState(error = e.message)) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun clearUnfinishedScan() {
        viewModelScope.launch {
            preferenceDataStore.setScanFolderPath(null)
        }
    }

    fun clearIndex(folderPath: String) {
        viewModelScope.launch {
            mediaRepository.clearIndexForFolder(folderPath)
        }
    }
}
