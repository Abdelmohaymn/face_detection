package com.example.facedetection.camera

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.facedetection.graphic.GraphicOverlay
import com.example.facedetection.graphic.LuminosityAnalyzer
import com.example.facedetection.R
import com.example.facedetection.detection.FaceDetectionModule
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService

/**
 * CameraModule is responsible for managing camera operations including
 * starting the camera preview and capturing photos.
 */
class CameraModule(
    private val context: Context,
    private val viewFinder: PreviewView,
    private val graphicOverlay: GraphicOverlay,
    private val cameraExecutor: ExecutorService,
    private val faceDetectionModule: FaceDetectionModule
) {

    private lateinit var imageCapture: ImageCapture

    /**
     * Starts the camera preview and sets up the image analyzer for face detection.
     */
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            // ImageCapture use case
            imageCapture = ImageCapture.Builder().build()

            // Select the back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // ImageAnalysis use case with a custom LuminosityAnalyzer
            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { bitmap ->
                    graphicOverlay.clear()
                    faceDetectionModule.detectFaces(bitmap, graphicOverlay)
                })
            }

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to the camera
                cameraProvider.bindToLifecycle(
                    context as AppCompatActivity, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraModule", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Captures a photo and saves it to the specified output directory.
     */
    fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraModule", "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Log.d("CameraModule", msg)
            }
        })
    }

    /**
     * Directory to save captured photos.
     */
    private val outputDirectory: File by lazy {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
