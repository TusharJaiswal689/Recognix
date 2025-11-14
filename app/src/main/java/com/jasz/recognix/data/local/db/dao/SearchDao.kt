package com.jasz.recognix.data.local.db.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.jasz.recognix.data.local.db.entity.ImageEntity

@Dao
interface SearchDao {
    @RawQuery
    suspend fun searchImages(query: SupportSQLiteQuery): List<ImageEntity>
}
