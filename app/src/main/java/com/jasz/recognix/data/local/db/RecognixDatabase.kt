package com.jasz.recognix.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jasz.recognix.data.local.db.dao.ImageDao
import com.jasz.recognix.data.local.db.entity.ImageEntity
import com.jasz.recognix.data.local.db.entity.ImageTagEntity

@Database(
    entities = [ImageEntity::class, ImageTagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RecognixDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}
