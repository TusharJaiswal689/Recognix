package com.jasz.recognix.workers

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jasz.recognix.data.local.db.dao.ImageDao
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.local.db.entity.ImageTagEntity
import com.jasz.recognix.ml.ObjectDetector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SaveEditedImageWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageDao: ImageDao,
    private val objectDetector: ObjectDetector
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val imageUriString = inputData.getString(KEY_IMAGE_URI) ?: return@withContext Result.failure()
        val imageUri = imageUriString.toUri()

        // 1. Add to MediaStore
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "EDITED_" + System.currentTimeMillis())
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val newItemUri = context.contentResolver.insert(collection, values) ?: return@withContext Result.failure()

        context.contentResolver.openOutputStream(newItemUri)?.use { outputStream ->
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(newItemUri, values, null, null)

        // 2. Save to our DB & Run Object Detection
        val pfd = context.contentResolver.openFileDescriptor(newItemUri, "r") ?: return@withContext Result.failure()
        pfd.use { descriptor ->
            val bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor)
            val tags = objectDetector.detect(bitmap)
            val imageEntity = ImageEntity(
                uri = newItemUri.toString(),
                folderPath = newItemUri.toString().substringBeforeLast('/'),
                lastModified = System.currentTimeMillis() / 1000
            )
            val imageTagEntities = tags.map { ImageTagEntity(imageUri = newItemUri.toString(), tag = it) }

            imageDao.upsertImage(imageEntity)
            imageDao.upsertTags(imageTagEntities)
        }

        // Optionally, delete original file if requested
        // val deleteOriginal = inputData.getBoolean(KEY_DELETE_ORIGINAL, false)
        // if (deleteOriginal) { context.contentResolver.delete(imageUri, null, null) }

        Result.success()
    }

    companion object {
        const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
    }
}
