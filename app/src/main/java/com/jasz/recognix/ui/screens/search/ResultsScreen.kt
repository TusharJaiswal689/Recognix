package com.jasz.recognix.ui.screens.search

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun ResultsScreen(navController: NavController) {
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        // placeholder items
    }
}
