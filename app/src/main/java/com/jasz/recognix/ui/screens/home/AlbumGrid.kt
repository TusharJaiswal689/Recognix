package com.jasz.recognix.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jasz.recognix.data.model.Album
import com.jasz.recognix.utils.formatSize
import java.net.URLEncoder

@Composable
fun AlbumGrid(albums: List<Album>, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(4.dp)
    ) {
        items(albums) { album ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { 
                        val encodedPath = URLEncoder.encode(album.path, "UTF-8")
                        val encodedLabel = URLEncoder.encode(album.label, "UTF-8")
                        navController.navigate("album/$encodedPath/$encodedLabel")
                    }
            ) {
                Column {
                    AsyncImage(
                        model = album.uri,
                        contentDescription = album.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = album.label, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${album.itemCount} items • ${formatSize(album.size)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
