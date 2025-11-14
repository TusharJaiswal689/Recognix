package com.jasz.recognix.ui.screens.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jasz.recognix.utils.startScan
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    albumPath: String,
    albumLabel: String,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showClearIndexDialog by remember { mutableStateOf(false) }

    if (showClearIndexDialog) {
        AlertDialog(
            onDismissRequest = { showClearIndexDialog = false },
            title = { Text("Clear Index") },
            text = { Text("Are you sure you want to clear the index for this album? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearIndex()
                        showClearIndexDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearIndexDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(albumLabel) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearIndexDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Index")
                    }
                    IconButton(onClick = { startScan(context, albumPath) }) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "Scan")
                    }
                    IconButton(onClick = {
                        val encodedPath = URLEncoder.encode(albumPath, "UTF-8")
                        navController.navigate("search/$encodedPath")
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}")
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp)) {
                    items(uiState.images) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.clickable { 
                                val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                                navController.navigate("viewer/$encodedUri") 
                            }
                        )
                    }
                }
            }
        }
    }
}
