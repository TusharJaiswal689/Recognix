package com.jasz.recognix.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import com.jasz.recognix.data.local.db.dao.ImageDao
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val imageDao: ImageDao
) {

    fun getAlbums(): Flow<List<Album>> = flow {
        val albums = mutableMapOf<Long, Album>()
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val imageIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getLong(bucketIdColumn)
                val size = cursor.getLong(sizeColumn)
                val album = albums[bucketId]
                if (album == null) {
                    val bucketName = cursor.getString(bucketNameColumn)
                    val imageId = cursor.getLong(imageIdColumn)
                    val imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId.toString())
                    val path = cursor.getString(dataColumn).substringBeforeLast('/')
                    albums[bucketId] = Album(bucketId, bucketName, imageUri, 1, size, path)
                } else {
                    albums[bucketId] = album.copy(
                        count = album.count + 1,
                        size = album.size + size
                    )
                }
            }
        }
        emit(albums.values.toList())
    }.flowOn(Dispatchers.IO)

    suspend fun getImagesForAlbum(albumPath: String): List<Uri> = withContext(Dispatchers.IO) {
        val images = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$albumPath%")

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                images.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()))
            }
        }
        images
    }

    fun searchImages(query: String, currentFolder: String): Flow<List<ImageEntity>> {
        val tags = query.split(",", ".").map { it.trim() }.filter { it.isNotEmpty() }
        val folder = if (currentFolder == "/") "" else currentFolder // Use "" for root, which will match all paths with LIKE
        return imageDao.findByTags(folder, tags)
    }

    suspend fun getImageByUri(uri: String): ImageEntity? = withContext(Dispatchers.IO) {
        imageDao.getImageByUri(uri)
    }

    suspend fun deleteImage(uri: Uri) = withContext(Dispatchers.IO) {
        contentResolver.delete(uri, null, null)
        imageDao.deleteTagsForImage(uri.toString())
        imageDao.deleteImage(uri.toString())
    }

    suspend fun renameImage(uri: Uri, newName: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, newName)
        }
        contentResolver.update(uri, values, null, null)
        // The image URI in our DB doesn't change, so we don't need to update it.
    }

    suspend fun clearIndexForFolder(folderPath: String) = withContext(Dispatchers.IO) {
        val folder = if (folderPath == "/") "" else folderPath
        imageDao.clearTagsForFolder(folder)
        imageDao.clearImagesForFolder(folder)
    }
}
