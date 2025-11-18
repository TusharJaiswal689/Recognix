package com.jasz.recognix.ml

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecognixObjectDetector @Inject constructor(@ApplicationContext private val context: Context) {

    private var detector: ObjectDetector? = null

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded = _isLoaded.asStateFlow()

    init {
        setup()
    }

    private fun setup() {
        try {
            val baseOptions = BaseOptions.builder().useGpu().build()
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setScoreThreshold(SCORE_THRESHOLD)
                .setMaxResults(MAX_RESULTS)
                .build()
            detector = ObjectDetector.createFromFileAndOptions(context, MODEL_NAME, options)
            _isLoaded.value = true
        } catch (e: IOException) {
            _isLoaded.value = false
        } catch (e: IllegalStateException) {
            _isLoaded.value = false
        }
    }

    fun detect(bitmap: Bitmap): List<String> {
        if (detector == null) return emptyList()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val results: List<Detection>? = detector?.detect(tensorImage)

        return results?.mapNotNull { detection ->
            detection.categories.firstOrNull()?.label?.substringAfter(' ')
        } ?: emptyList()
    }

    companion object {
        private const val MODEL_NAME = "ssd-mobilenet-v2.tflite"
        private const val SCORE_THRESHOLD = 0.5f
        private const val MAX_RESULTS = 5
    }
}
