package com.jasz.recognix.utils

import android.content.Context
import android.net.Uri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.jasz.recognix.workers.SaveEditedImageWorker

fun startSaveEditedImageWorker(context: Context, uri: Uri) {
    val workManager = WorkManager.getInstance(context)
    val workRequest = OneTimeWorkRequestBuilder<SaveEditedImageWorker>()
        .setInputData(workDataOf(SaveEditedImageWorker.KEY_IMAGE_URI to uri.toString()))
        .build()
    workManager.enqueue(workRequest)
}
