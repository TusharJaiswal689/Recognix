package com.jasz.recognix.scan

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ScanWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    @ApplicationContext private val context: Context
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Placeholder: call ScanManager to do actual scan
        // val folder = inputData.getString("folder") ?: return Result.failure()
        // ScanManager.startScan(folder)
        return Result.success()
    }
}
