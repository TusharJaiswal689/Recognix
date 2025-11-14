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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val imageDao: ImageDao
) {

    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        // ... (getAlbums implementation remains the same)
    }

    suspend fun getImagesForAlbum(albumPath: String): List<Uri> = withContext(Dispatchers.IO) {
        // ... (getImagesForAlbum implementation remains the same)
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
