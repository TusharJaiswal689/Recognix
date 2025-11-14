package com.jasz.recognix.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jasz.recognix.R
import com.jasz.recognix.utils.RequestMediaPermission
import com.jasz.recognix.utils.startScan
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var showClearIndexDialog by remember { mutableStateOf(false) }

    RequestMediaPermission(
        onPermissionGranted = { viewModel.loadAlbums() },
        onPermissionDenied = { /* TODO: Show a message to the user */ }
    )

    if (showClearIndexDialog) {
        AlertDialog(
            onDismissRequest = { showClearIndexDialog = false },
            title = { Text("Clear Index") },
            text = { Text("Are you sure you want to clear the index for the entire gallery? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearIndex("/")
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

    uiState.unfinishedScanPath?.let { path ->
        AlertDialog(
            onDismissRequest = { viewModel.clearUnfinishedScan() },
            title = { Text("Resume Scan") },
            text = { Text("An unfinished scan was found for the folder: $path. Do you want to resume?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        startScan(context, path)
                        viewModel.clearUnfinishedScan()
                    }
                ) {
                    Text("Resume")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearUnfinishedScan() }) {
                    Text("No")
                }
            }
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recognix") },
                actions = {
                    IconButton(onClick = { showClearIndexDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Index")
                    }
                    IconButton(onClick = { startScan(context, "/") }) {
                        Icon(painterResource(id = R.drawable.ic_scan), contentDescription = "Scan")
                    }
                    IconButton(onClick = { 
                        val encodedPath = URLEncoder.encode("/", "UTF-8")
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
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = "Error: ${uiState.error}")
                }
                else -> {
                    AlbumGrid(albums = uiState.albums, navController = navController)
                }
            }
        }
    }
}
