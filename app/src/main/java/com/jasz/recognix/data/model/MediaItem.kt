package com.jasz.recognix.data.model

import android.net.Uri

enum class MediaType {
    IMAGE, VIDEO
}

data class MediaItem(
    val uri: Uri,
    val type: MediaType
)
