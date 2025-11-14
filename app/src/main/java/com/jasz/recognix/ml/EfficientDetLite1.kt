package com.jasz.recognix.ml

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.MappedByteBuffer

class EfficientDetTagger(
    private val context: Context,
    private val modelAssetPath: String,
    private val labelAssetPath: String
) : ImageTagger {

    private val interpreter: Interpreter
    private val labels: List<String>

    init {
        val model: MappedByteBuffer = FileUtil.loadMappedFile(context, modelAssetPath)
        interpreter = Interpreter(model)
        labels = FileUtil.loadLabels(context, labelAssetPath)
    }

    override suspend fun detect(bitmap: Bitmap): List<Detection> = withContext(Dispatchers.Default) {
        // Very minimal placeholder: in practice use TFLite Task Library or parse outputs correctly
        // Here return empty list so app runs until we implement proper postprocessing
        emptyList()
    }

    override fun close() {
        interpreter.close()
    }
}
