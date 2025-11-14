package com.jasz.recognix.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val uri: String,
    @ColumnInfo(name = "folder_path") val folderPath: String,
    @ColumnInfo(name = "last_modified") val lastModified: Long
)
