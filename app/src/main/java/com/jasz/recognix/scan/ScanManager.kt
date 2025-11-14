package com.jasz.recognix.scan

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanManager @Inject constructor(
    private val context: Context
) {
    suspend fun startScan(folderPath: String) {
        // Implement MediaStore traversal, model inference per image, and persist via repository
    }

    suspend fun resumeScanIfNeeded() {
        // implement loading scanState and resuming
    }
}
