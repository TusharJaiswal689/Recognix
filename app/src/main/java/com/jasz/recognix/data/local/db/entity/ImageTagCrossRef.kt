package com.jasz.recognix.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "image_tag_cross_ref", primaryKeys = ["uri", "tag"])
data class ImageTagCrossRef(
    val uri: String,
    @ColumnInfo(index = true) val tag: String
)
