package com.jasz.recognix.ui.screens.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jasz.recognix.data.model.MediaType
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
    var showMenu by remember { mutableStateOf(false) }
    var showClearIndexDialog by remember { mutableStateOf(false) }

    if (showClearIndexDialog) {
        AlertDialog(
            onDismissRequest = { showClearIndexDialog = false },
            title = { Text("Clear Index") },
            text = { Text("Are you sure you want to clear the index for this album? This action cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.clearIndex(); showClearIndexDialog = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { showClearIndexDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(albumLabel) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        val encodedPath = URLEncoder.encode(albumPath, "UTF-8")
                        navController.navigate("search/$encodedPath")
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Scan") }, onClick = { startScan(context, albumPath); showMenu = false })
                        DropdownMenuItem(text = { Text("Clear Index") }, onClick = { showClearIndexDialog = true; showMenu = false })
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
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(text = "Error: ${uiState.error}")
                else -> {
                    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp)) {
                        items(uiState.media) { item ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { 
                                        if (item.type == MediaType.IMAGE) {
                                            val encodedUri = URLEncoder.encode(item.uri.toString(), "UTF-8")
                                            navController.navigate("viewer/$encodedUri") 
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (item.type == MediaType.VIDEO) {
                                    Icon(
                                        imageVector = Icons.Outlined.PlayCircleOutline,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxSize(0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
