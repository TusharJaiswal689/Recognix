package com.jasz.recognix.data.local.entities

import androidx.room.Entity

@Entity(tableName = "image_tags", primaryKeys = ["imageUri", "tag"])
data class ImageTagEntity(
    val imageUri: String,
    val tag: String,
    val confidence: Float
)
