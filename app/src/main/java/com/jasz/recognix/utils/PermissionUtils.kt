package com.jasz.recognix.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

@Composable
fun RequestMediaPermission(onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            hasPermission = it
            if (it) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    if (hasPermission) {
        onPermissionGranted()
    }
}
