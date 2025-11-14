package com.jasz.recognix.ml

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
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
class ObjectDetector @Inject constructor(@ApplicationContext private val context: Context) {

    private var detector: ObjectDetector? = null

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
        } catch (e: IOException) {
            // TODO: Log this error to Crashlytics
        } catch (e: IllegalStateException) {
            // TODO: Log this error to Crashlytics
        }
    }

    fun detect(bitmap: Bitmap): List<String> {
        if (detector == null) {
            // This should not happen if the init block is successful, but as a fallback:
            setup()
        }

        val imageProcessor = ImageProcessor.builder()
            .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val results: List<Detection>? = detector?.detect(tensorImage)

        return results?.mapNotNull { detection ->
            detection.categories.firstOrNull()?.label?.substringAfter(' ')
        } ?: emptyList()
    }

    companion object {
        private const val MODEL_NAME = "efficientdet-lite1.tflite"
        private const val SCORE_THRESHOLD = 0.5f
        private const val MAX_RESULTS = 5
    }
}
