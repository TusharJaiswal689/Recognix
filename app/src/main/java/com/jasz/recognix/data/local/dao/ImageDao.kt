package com.jasz.recognix.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.jasz.recognix.data.local.entities.ImageEntity
import com.jasz.recognix.data.local.entities.ImageTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertImage(image: ImageEntity)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertTags(tags: List<ImageTagEntity>)

    @Query("SELECT * FROM images WHERE uri = :uri")
    suspend fun getImage(uri: String): ImageEntity?

    // Query images by tag(s) within a folder (folderPath can be '%' for root)
    @Query("""
        SELECT i.* FROM images i
        JOIN image_tags t ON i.uri = t.imageUri
        WHERE (:folderRoot IS NULL OR i.folderPath LIKE :folderRoot || '%')
        AND t.tag IN (:tags)
        GROUP BY i.uri
    """)
    fun findByTags(folderRoot: String?, tags: List<String>): Flow<List<ImageEntity>>
}
