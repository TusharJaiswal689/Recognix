package com.jasz.recognix.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val uri: String,    // use MediaStore URI as unique id
    val displayName: String?,
    val folderPath: String?,
    val width: Int?,
    val height: Int?,
    val size: Long?,
    val lastModified: Long?
)
