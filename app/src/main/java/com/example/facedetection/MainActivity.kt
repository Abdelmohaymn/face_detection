package com.example.facedetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facedetection.camera.CameraModule
import com.example.facedetection.detection.FaceDetectionModule
import com.example.facedetection.graphic.GraphicOverlay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * MainActivity is the main entry point of the application, handling UI interactions
 * and managing camera and face detection functionalities.
 */
class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var viewFinder: PreviewView
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var captureButton: Button

    // Executors for background tasks
    private lateinit var cameraExecutor: ExecutorService

    // Camera and face detection modules
    private lateinit var cameraModule: CameraModule
    private lateinit var faceDetectionModule: FaceDetectionModule

    // Permission request codes and required permissions
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        viewFinder = findViewById(R.id.viewFinder)
        graphicOverlay = findViewById(R.id.graphic_overlay)
        captureButton = findViewById(R.id.captureButton)

        // Initialize the executor for camera operations
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize face detection and camera modules
        faceDetectionModule = FaceDetectionModule(this)
        cameraModule = CameraModule(this, viewFinder, graphicOverlay, cameraExecutor, faceDetectionModule)

        // Check for camera permissions and start the camera
        if (allPermissionsGranted()) {
            cameraModule.startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the capture button to take photos
        captureButton.setOnClickListener {
            cameraModule.takePhoto()
        }
    }

    /**
     * Checks if all required permissions are granted.
     *
     * @return true if all permissions are granted, false otherwise.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Handles the result of the permission request.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraModule.startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
