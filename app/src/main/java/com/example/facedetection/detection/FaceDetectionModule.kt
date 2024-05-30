package com.example.facedetection.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.example.facedetection.graphic.FaceContourGraphic
import com.example.facedetection.graphic.GraphicOverlay
import com.example.facedetection.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

/**
 * FaceDetectionModule is responsible for handling face detection
 * using a pre-trained TensorFlow Lite model.
 */
class FaceDetectionModule(context: Context) {

    private val labels: List<String> = FileUtil.loadLabels(context, "labels.txt")
    private val model: SsdMobilenetV11Metadata1 = SsdMobilenetV11Metadata1.newInstance(context)
    private val imageProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    /**
     * Detects faces in the given bitmap image and adds them to the graphic overlay.
     *
     * @param bitmap The bitmap image in which faces need to be detected.
     * @param graphicOverlay The overlay on which detected faces will be drawn.
     */
    fun detectFaces(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray

        val h = bitmap.height
        val w = bitmap.width

        scores.forEachIndexed { index, score ->
            if (score > 0.5 && labels[classes[index].toInt()] == "person") {
                val x = index * 4
                val rect = RectF(
                    locations[x + 1] * w, locations[x] * h,
                    locations[x + 3] * w, locations[x + 2] * h
                )
                val faceGraphic = FaceContourGraphic(graphicOverlay, rect)
                graphicOverlay.add(faceGraphic)
            }
        }
    }
}
