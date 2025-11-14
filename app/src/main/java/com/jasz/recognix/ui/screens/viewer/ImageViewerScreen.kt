package com.jasz.recognix.ui.screens.viewer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.jasz.recognix.utils.startSaveEditedImageWorker
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    navController: NavController,
    viewModel: ViewerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteImage()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val cropActivityLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                startSaveEditedImageWorker(context, uri)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            if (route == "-1") {
                navController.popBackStack()
            } else {
                navController.navigate(route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.imageName ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Jump to location") },
                            onClick = { 
                                viewModel.onJumpToLocationClicked()
                                showMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                val cropOptions = CropImageContractOptions(viewModel.imageUri, CropImageOptions())
                                cropActivityLauncher.launch(cropOptions)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { 
                                showDeleteDialog = true
                                showMenu = false 
                            }
                        )
                        // TODO: Add other menu items (Info, Rename, etc.)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AsyncImage(
                model = viewModel.imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
