package com.jasz.recognix.ml

import android.graphics.Bitmap

data class Detection(val label: String, val score: Float, val bbox: FloatArray)

interface ImageTagger {
    suspend fun detect(bitmap: Bitmap): List<Detection>
    fun close()
}
