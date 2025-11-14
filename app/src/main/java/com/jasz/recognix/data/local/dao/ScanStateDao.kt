package com.jasz.recognix.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jasz.recognix.data.local.entities.ScanStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ScanStateEntity)

    @Query("SELECT * FROM scan_state WHERE folderPath = :folder")
    suspend fun getState(folder: String): ScanStateEntity?

    @Query("DELETE FROM scan_state WHERE folderPath = :folder")
    suspend fun deleteState(folder: String)
}
