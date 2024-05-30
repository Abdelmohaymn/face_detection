package com.example.facedetection.graphic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * LuminosityAnalyzer is responsible for analyzing image frames from the camera and converting
 * them to a Bitmap for further processing.
 *
 * @param listener A callback that receives the Bitmap representation of the analyzed image.
 */
class LuminosityAnalyzer(private val listener: (Bitmap) -> Unit) : ImageAnalysis.Analyzer {

    /**
     * Analyzes the image and passes the converted Bitmap to the listener.
     *
     * @param imageProxy The image to be analyzed.
     */
    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = toBitmap(imageProxy)
        listener(bitmap)
        imageProxy.close()
    }

    /**
     * Converts an ImageProxy to a Bitmap.
     *
     * @param imageProxy The image to be converted.
     * @return The Bitmap representation of the image.
     */
    private fun toBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // Allocate a byte array for the NV21 image format
        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy the YUV data into the NV21 byte array
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        // Create a YuvImage object and compress it to JPEG format
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()

        // Convert the JPEG byte array to a Bitmap
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
