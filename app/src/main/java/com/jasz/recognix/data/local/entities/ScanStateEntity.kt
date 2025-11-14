package com.jasz.recognix.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_state")
data class ScanStateEntity(
    @PrimaryKey val folderPath: String, // folder being scanned
    val lastProcessedUri: String?,      // resume point (MediaStore uri)
    val inProgress: Boolean,
    val progressCount: Int
)
