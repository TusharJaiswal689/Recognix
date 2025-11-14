package com.jasz.recognix.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.jasz.recognix.workers.ScanWorker

fun startScan(context: Context, folderPath: String) {
    val workManager = WorkManager.getInstance(context)
    val workRequest = OneTimeWorkRequestBuilder<ScanWorker>()
        .setInputData(workDataOf(ScanWorker.KEY_FOLDER_PATH to folderPath))
        .build()
    workManager.enqueue(workRequest)
}
