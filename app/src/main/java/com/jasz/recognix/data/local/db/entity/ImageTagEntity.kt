package com.jasz.recognix.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "image_tags", primaryKeys = ["imageUri", "tag"])
data class ImageTagEntity(
    @ColumnInfo(index = true) val imageUri: String,
    @ColumnInfo(index = true) val tag: String
)
