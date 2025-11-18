package com.jasz.recognix.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jasz.recognix.R
import com.jasz.recognix.utils.RequestMediaPermission
import com.jasz.recognix.utils.startScan
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showClearIndexDialog by remember { mutableStateOf(false) }

    RequestMediaPermission(onPermissionGranted = { /* ViewModel now handles loading */ }, onPermissionDenied = { /* TODO */ })

    LaunchedEffect(viewModel) {
        viewModel.uiState
            .filterNotNull()
            .distinctUntilChanged { old, new -> old.isModelLoaded == new.isModelLoaded }
            .onEach { state ->
                val message = if (state.isModelLoaded == true) "ML Model Loaded Successfully" else "ML Model Failed to Load"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            .launchIn(this)
    }

    if (showClearIndexDialog) {
        AlertDialog(
            onDismissRequest = { showClearIndexDialog = false },
            title = { Text("Clear Index") },
            text = { Text("Are you sure you want to clear the index for the entire gallery? This action cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.clearIndex("/"); showClearIndexDialog = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { showClearIndexDialog = false }) { Text("Cancel") } }
        )
    }

    uiState.unfinishedScanPath?.let { path ->
        AlertDialog(
            onDismissRequest = { viewModel.clearUnfinishedScan() },
            title = { Text("Resume Scan") },
            text = { Text("An unfinished scan was found for the folder: $path. Do you want to resume?") },
            confirmButton = { TextButton(onClick = { startScan(context, path); viewModel.clearUnfinishedScan() }) { Text("Resume") } },
            dismissButton = { TextButton(onClick = { viewModel.clearUnfinishedScan() }) { Text("No") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Image(painter = painterResource(id = R.drawable.rx_logo), contentDescription = "Recognix Logo", modifier = Modifier.height(40.dp)) },
                actions = {
                    IconButton(onClick = { 
                        val encodedPath = URLEncoder.encode("/", "UTF-8")
                        navController.navigate("search/$encodedPath") 
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Scan") }, onClick = { startScan(context, "/"); showMenu = false })
                        DropdownMenuItem(text = { Text("Clear Index") }, onClick = { showClearIndexDialog = true; showMenu = false })
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}")
            } else {
                AlbumGrid(albums = uiState.albums, navController = navController)
            }
        }
    }
}
