package com.jasz.recognix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.jasz.recognix.ui.screens.home.HomeScreen
import com.jasz.recognix.ui.screens.search.ResultsScreen
import com.jasz.recognix.ui.screens.viewer.ImageViewerScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("results") { ResultsScreen() }
        composable("viewer/{uri}") {
            // The ViewModel will extract the URI from the backStackEntry's arguments
            ImageViewerScreen(navController)
        }
    }
}
