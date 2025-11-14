package com.jasz.recognix.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jasz.recognix.data.local.datastore.PreferenceDataStore
import com.jasz.recognix.data.local.db.dao.ImageDao
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.local.db.entity.ImageTagEntity
import com.jasz.recognix.ml.RecognixObjectDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class ScanWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageDao,
    private val recognixObjectDetector: RecognixObjectDetector,
    private val preferenceDataStore: PreferenceDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val folderPath = inputData.getString(KEY_FOLDER_PATH) ?: return@withContext Result.failure()
        preferenceDataStore.setScanFolderPath(folderPath)

        val lastScanTimestamp = preferenceDataStore.scanFolderPath.first()?.let { preferenceDataStore.getLastScanTimestamp(it) } ?: 0L

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val selection = "${MediaStore.Images.Media.DATA} LIKE ? AND ${MediaStore.Images.Media.DATE_MODIFIED} > ?"
        val selectionArgs = arrayOf("$folderPath%", lastScanTimestamp.toString())

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified = cursor.getLong(dateModifiedColumn)
                val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: continue
                pfd.use { descriptor ->
                    val bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor)
                    val tags = recognixObjectDetector.detect(bitmap)
                    val imageEntity = ImageEntity(
                        uri = uri.toString(),
                        displayName = cursor.getString(displayNameColumn),
                        folderPath = folderPath,
                        width = cursor.getInt(widthColumn),
                        height = cursor.getInt(heightColumn),
                        size = cursor.getLong(sizeColumn),
                        lastModified = dateModified
                    )
                    val imageTagEntities = tags.map { ImageTagEntity(imageUri = uri.toString(), tag = it) }

                    imageDao.upsertImage(imageEntity)
                    imageDao.upsertTags(imageTagEntities)
                    preferenceDataStore.setLastScanTimestamp(folderPath, dateModified)
                }
            }
        }

        preferenceDataStore.setScanFolderPath(null)
        Result.success()
    }

    companion object {
        const val KEY_FOLDER_PATH = "KEY_FOLDER_PATH"
    }
}
