package com.jasz.recognix.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val label: String,
    val uri: Uri,       // URI of the most recent item for the cover
    val itemCount: Int, // Combined count of images and videos
    val size: Long,     // Combined size of all items
    val path: String
)
