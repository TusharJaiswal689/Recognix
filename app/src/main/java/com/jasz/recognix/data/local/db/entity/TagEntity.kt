package com.jasz.recognix.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val tag: String
)
