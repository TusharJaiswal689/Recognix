package com.jasz.recognix.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.local.db.entity.ImageTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Upsert
    suspend fun upsertImage(image: ImageEntity)

    @Upsert
    suspend fun upsertTags(tags: List<ImageTagEntity>)

    @Query("SELECT * FROM images WHERE folderPath LIKE :folderPath || '%' AND uri IN (SELECT imageUri FROM image_tags WHERE tag IN (:tags)) ORDER BY lastModified DESC")
    fun findByTags(folderPath: String, tags: List<String>): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE uri = :uri")
    suspend fun getImageByUri(uri: String): ImageEntity?

    @Query("DELETE FROM image_tags WHERE imageUri = :imageUri")
    suspend fun deleteTagsForImage(imageUri: String)

    @Query("DELETE FROM images WHERE uri = :imageUri")
    suspend fun deleteImage(imageUri: String)

    @Query("DELETE FROM image_tags WHERE imageUri IN (SELECT uri FROM images WHERE folderPath LIKE :folderPath || '%')")
    suspend fun clearTagsForFolder(folderPath: String)

    @Query("DELETE FROM images WHERE folderPath LIKE :folderPath || '%'")
    suspend fun clearImagesForFolder(folderPath: String)
}
