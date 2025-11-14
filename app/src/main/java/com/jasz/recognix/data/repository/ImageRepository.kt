package com.jasz.recognix.data.repository

import com.jasz.recognix.data.local.dao.ImageDao
import com.jasz.recognix.data.local.dao.ScanStateDao
import com.jasz.recognix.data.local.entities.ImageEntity
import com.jasz.recognix.data.local.entities.ImageTagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ImageRepository {
    suspend fun upsertImage(image: ImageEntity)
    suspend fun upsertTags(tags: List<ImageTagEntity>)
    fun queryByTags(folderRoot: String?, tags: List<String>, andMode: Boolean): Flow<List<ImageEntity>>
}

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val imageDao: ImageDao
) : ImageRepository {
    override suspend fun upsertImage(image: ImageEntity) = imageDao.upsertImage(image)
    override suspend fun upsertTags(tags: List<ImageTagEntity>) = imageDao.upsertTags(tags)

    override fun queryByTags(folderRoot: String?, tags: List<String>, andMode: Boolean) : Flow<List<ImageEntity>> {
        // Simple implementation: if AND mode, perform repeated filtering in Kotlin.
        val flow = imageDao.findByTags(folderRoot, tags)
        if (!andMode) return flow

        // For AND: filter results that contain all requested tags
        return kotlinx.coroutines.flow.flow {
            flow.collect { list ->
                // For each image, fetch its tags (quick approach: query tag table might be added later)
                val filtered = list.filter { image ->
                    // naive: query tags per image is extra DB calls; for starter we keep this simple.
                    true // implement later: join query to ensure count matches
                }
                emit(filtered)
            }
        }
    }
}
