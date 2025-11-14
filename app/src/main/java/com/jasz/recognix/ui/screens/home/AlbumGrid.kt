package com.jasz.recognix.ui.screens.home

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jasz.recognix.data.model.Album
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGrid(albums: List<Album>, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        items(albums) { album ->
            Card(
                onClick = { 
                    val encodedPath = URLEncoder.encode(album.path, "UTF-8")
                    val encodedLabel = URLEncoder.encode(album.label, "UTF-8")
                    navController.navigate("album/$encodedPath/$encodedLabel") 
                },
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = album.uri,
                    contentDescription = album.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Text(text = album.label, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
