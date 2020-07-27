package com.app.cameraxview

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName
    var isRecording = false

    var CAMERA_PERMISSION = Manifest.permission.CAMERA
    var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    var RC_PERMISSION = 101
    var thumbnail: ImageView? = null
    var imagePreview: ConstraintLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recordFiles = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MOVIES)
        val storageDirectory = recordFiles[0]
        val imageCaptureFilePath =
            "${storageDirectory.absoluteFile}/${System.currentTimeMillis()}_image.jpg"

        if (checkPermissions()) startCameraSession() else requestPermissions()
        dialogbox()
        capture_image.setOnClickListener {
            captureImage(imageCaptureFilePath)
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION),
            RC_PERMISSION
        )
    }

    private fun checkPermissions(): Boolean {
        return ((ActivityCompat.checkSelfPermission(
            this,
            CAMERA_PERMISSION
        )) == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(
            this,
            CAMERA_PERMISSION
        )) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_PERMISSION -> {
                var allPermissionsGranted = false
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    } else {
                        allPermissionsGranted = true
                    }
                }
                if (allPermissionsGranted) startCameraSession() else permissionsNotGranted()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSession() {
        camera_view.bindToLifecycle(this)
    }

    private fun permissionsNotGranted() {
        AlertDialog.Builder(this).setTitle("Permissions required")
            .setMessage("These permissions are required to use this app. Please allow Camera and Audio permissions first")
            .setCancelable(false)
            .setPositiveButton("Grant") { dialog, which -> requestPermissions() }
            .show()
    }


    private fun captureImage(imageCaptureFilePath: String) {
        camera_view.takePicture(
            File(imageCaptureFilePath),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "Image Captured", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onImageSaved $imageCaptureFilePath")


                    thumbnail = findViewById<ImageView>(R.id.image_view)
                    imagePreview = findViewById(R.id.cl_imagePreview)
                    val closePreview: Button = findViewById(R.id.closeButton)

                    closePreview.setOnClickListener(View.OnClickListener {
                        thumbnail?.setImageBitmap(null)
                      showIconWhenAppIsNotShowingImage()
                    })

                    save_image.setOnClickListener(View.OnClickListener {
                        Toast.makeText(this@MainActivity,"onImageSaved $imageCaptureFilePath",Toast.LENGTH_LONG).show()
                    })
                    // Run the operations in the view's thread
                    thumbnail?.post {
                      showIconWhenAppShowingImage()
                        // Remove thumbnail padding
                        thumbnail?.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

                        updatePreview(imageCaptureFilePath)
                        dialogbox()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Image Capture Failed", Toast.LENGTH_SHORT)
                        .show()
                    Log.e(TAG, "onError $exception")

                }
            })
    }

    fun dialogbox() {
        val builder: AlertDialog.Builder? = this.let {
            AlertDialog.Builder(it) }
        builder!!.setMessage("Are you sure you want to log out?")
            .setTitle("Log Out")
        builder.apply {
            setNegativeButton("YES") { dialog, id ->
                val selectedId = id } }
        val dialog: AlertDialog? = builder.create()
        dialog!!.show()
    }
    fun updatePreview(imageCaptureFilePath: String){
        // Load thumbnail into circular button using Glide
        Glide.with(thumbnail!!)
            .load(imageCaptureFilePath)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(thumbnail!!)
    }

   fun showIconWhenAppShowingImage(){
       save_image.visibility=View.VISIBLE
       imagePreview?.visibility=View.VISIBLE
       capture_image.visibility=View.GONE
       camera_view.visibility=View.GONE
   }
    fun showIconWhenAppIsNotShowingImage(){
        save_image.visibility=View.INVISIBLE
        imagePreview?.visibility=View.GONE
        capture_image.visibility=View.VISIBLE
        camera_view.visibility=View.VISIBLE
    }
}
