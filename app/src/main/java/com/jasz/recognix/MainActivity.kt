package com.jasz.recognix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jasz.recognix.ui.screens.album.AlbumScreen
import com.jasz.recognix.ui.screens.home.HomeScreen
import com.jasz.recognix.ui.screens.search.SearchScreen
import com.jasz.recognix.ui.screens.viewer.ImageViewerScreen
import com.jasz.recognix.ui.theme.RecognixTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            RecognixTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("album/{albumPath}/{albumLabel}") { backStackEntry ->
            val albumPath = backStackEntry.arguments?.getString("albumPath")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            val albumLabel = backStackEntry.arguments?.getString("albumLabel")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            AlbumScreen(navController, albumPath, albumLabel)
        }
        composable("search/{folderPath}") { backStackEntry ->
            val folderPath = backStackEntry.arguments?.getString("folderPath")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            SearchScreen(navController, currentFolder = folderPath)
        }
        composable("viewer/{imageUri}") { 
            ImageViewerScreen(navController)
        }
    }
}
