package com.jasz.recognix.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.net.URLEncoder

@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel = hiltViewModel(), currentFolder: String) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChanged(it) },
            label = { Text("Search with tags (e.g., cat.dog,bird)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchImages(currentFolder) })
        )

        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp)) {
            items(uiState.results) { image ->
                AsyncImage(
                    model = image.uri,
                    contentDescription = null,
                    modifier = Modifier.clickable { 
                        val encodedUri = URLEncoder.encode(image.uri, "UTF-8")
                        navController.navigate("viewer/$encodedUri") 
                    }
                )
            }
        }
    }
}
