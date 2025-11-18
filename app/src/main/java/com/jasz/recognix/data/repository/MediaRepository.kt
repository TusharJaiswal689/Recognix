package com.jasz.recognix.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import com.jasz.recognix.data.local.db.dao.ImageDao
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.model.Album
import com.jasz.recognix.data.model.MediaItem
import com.jasz.recognix.data.model.MediaType
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
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?)"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getLong(bucketIdColumn)
                val size = cursor.getLong(sizeColumn)
                val album = albums[bucketId]

                // Use the most recent item as the album cover
                if (album == null) {
                    val id = cursor.getLong(idColumn)
                    val bucketName = cursor.getString(bucketNameColumn)
                    val itemUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    val path = cursor.getString(dataColumn).substringBeforeLast('/')
                    albums[bucketId] = Album(bucketId, bucketName, itemUri, 1, size, path)
                } else {
                    albums[bucketId] = album.copy(
                        itemCount = album.itemCount + 1,
                        size = album.size + size
                    )
                }
            }
        }
        emit(albums.values.toList())
    }.flowOn(Dispatchers.IO)

    fun getMediaForAlbum(albumPath: String): Flow<List<MediaItem>> = flow {
        val media = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ? AND (${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?)"
        val selectionArgs = arrayOf("$albumPath%", MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(), MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val type = when (cursor.getInt(mediaTypeColumn)) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaType.IMAGE
                    else -> MediaType.VIDEO
                }
                val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                media.add(MediaItem(uri, type))
            }
        }
        emit(media)
    }.flowOn(Dispatchers.IO)

    fun searchImages(query: String, currentFolder: String): Flow<List<ImageEntity>> = imageDao.findByTags(if (currentFolder == "/") "" else currentFolder, query.split(",", ".").map { it.trim() }.filter { it.isNotEmpty() })

    fun getImageByUri(uri: String): Flow<ImageEntity?> = imageDao.getImageByUri(uri)

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
    }

    suspend fun clearIndexForFolder(folderPath: String) = withContext(Dispatchers.IO) {
        val folder = if (folderPath == "/") "" else folderPath
        imageDao.clearTagsForFolder(folder)
        imageDao.clearImagesForFolder(folder)
    }
}
