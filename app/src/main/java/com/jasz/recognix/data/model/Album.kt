package com.jasz.recognix.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val label: String,
    val uri: Uri,
    val count: Int,
    val size: Long,
    val path: String
)
