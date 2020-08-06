package com.app.cameraxview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationRequest
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName
    var isRecording = false
    var bag: CompositeDisposable = CompositeDisposable()

    var CAMERA_PERMISSION = Manifest.permission.CAMERA
    var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    var ACCESS_COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    var ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    var userLocation: Location? = null
    var RC_PERMISSION = 101
    var thumbnail: ImageView? = null
    var imagePreview: ConstraintLayout? = null
    var imageCaptureFilePath: String? = null

    companion object {
        var imageUrl: getImageValue? = null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (checkPermissions()) startCameraSession() else requestPermissions()
        capture_image.setOnClickListener {
            captureImage()
            getLocation(this@MainActivity, bag).observe(
                this@MainActivity,
                locationResponseLiveDataObserver()
            )

        }

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                CAMERA_PERMISSION,
                RECORD_AUDIO_PERMISSION,
                ACCESS_COARSE_LOCATION_PERMISSION,
                ACCESS_FINE_LOCATION_PERMISSION
            ),
            RC_PERMISSION
        )
    }

    private fun checkPermissions(): Boolean {
        return ((ActivityCompat.checkSelfPermission(
            this,
            CAMERA_PERMISSION
        )) == PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    CAMERA_PERMISSION
                )) == PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_COARSE_LOCATION_PERMISSION
                )) == PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_FINE_LOCATION_PERMISSION
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


    private fun captureImage() {

        val recordFiles = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MOVIES)
        val storageDirectory = recordFiles[0]
        imageCaptureFilePath =
            "${storageDirectory.absoluteFile}/${System.currentTimeMillis()}_image.jpg"

        camera_view.takePicture(File(imageCaptureFilePath), ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "Image Captured", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onImageSaved $imageCaptureFilePath")


                    thumbnail = findViewById<ImageView>(R.id.image_view)
                    imagePreview = findViewById(R.id.cl_imagePreview)
                    val closePreview: ImageView = findViewById(R.id.closeButton)

                    closePreview.setOnClickListener(View.OnClickListener {
                        thumbnail?.setImageBitmap(null)
                        showIconWhenAppIsNotShowingImage()
                        imageCaptureFilePath =
                            "${storageDirectory.absoluteFile}/${System.currentTimeMillis()}_image.jpg"

                    })

                    save_image.setOnClickListener(View.OnClickListener {

                        if (tv_location.text.toString().isBlank()) {

                            // Toast.makeText(this@MainActivity," wait location is not set with max accuracy",Toast.LENGTH_LONG).show()
                            dialogbox("wait location is not set with max accuracy")
                        } else {

                            Log.e("datata",""+imageCaptureFilePath)
                            imageUrl?.getUrl(imageCaptureFilePath!!, userLocation!!)
                            finish()
                        }

                        // Toast.makeText(this@MainActivity,"onImageSaved $imageCaptureFilePath",Toast.LENGTH_LONG).show()
                    })
                    // Run the operations in the view's thread
                    thumbnail?.post {
                        showIconWhenAppShowingImage()
                        // Remove thumbnail padding
                        thumbnail?.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())
                        // dialogbox("Test dialog")
                        updatePreview(imageCaptureFilePath!!)


                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Image Capture Failed", Toast.LENGTH_SHORT)
                        .show()
                    Log.e(TAG, "onError $exception")

                }
            })
    }

    inner class locationResponseLiveDataObserver :
        Observer<Location> {

        override fun onChanged(response: Location) {
            tv_location!!.text = "" + response.latitude + " == " + response.longitude
            userLocation = response
            Log.e("locationValue", "" + response.latitude + " " + response.longitude)
        }
    }

    fun dialogbox(msg: String) {
        val builder: AlertDialog.Builder? = this.let {
            AlertDialog.Builder(it)
        }
        builder!!.setMessage(msg)
            .setTitle("Dialog")
        builder.apply {
            setNegativeButton("YES") { dialog, id ->
                val selectedId = id
            }
        }
        val dialog: AlertDialog? = builder.create()
        dialog!!.show()
    }

    fun updatePreview(imageCaptureFilePath: String) {
        // Load thumbnail into circular button using Glide
        Glide.with(thumbnail!!)
            .load(imageCaptureFilePath)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(thumbnail!!)
    }

    fun showIconWhenAppShowingImage() {
        // save_image.visibility=View.VISIBLE
        imagePreview?.visibility = View.VISIBLE
        capture_image.visibility = View.GONE
        camera_view.visibility = View.GONE


    }

    fun showIconWhenAppIsNotShowingImage() {
        // save_image.visibility=View.INVISIBLE
        imagePreview?.visibility = View.GONE
        capture_image.visibility = View.VISIBLE
        camera_view.visibility = View.VISIBLE

    }


    interface getImageValue {
        fun getUrl(values: String, location: Location)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(context: Context): Observable<Location>? {
        val request = LocationRequest.create() //standard GMS LocationRequest
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(50).setInterval(20)

        val locationProvider = ReactiveLocationProvider(context)
        return locationProvider.getUpdatedLocation(request)
    }

    private fun getHighAccuracyLocation(
        context: Context,
        bag: CompositeDisposable
    ): Single<Location> {

        var location: Location? = null

        return Single.create { emitter ->

            bag.add(
                getCurrentLocation(context)!!.subscribe({


                    if (location == null) {
                        location = it
                    } else {
                        if (location!!.accuracy > it.accuracy) {
                            location = it
                        }
                    }

                    bag.add(

                        Completable.timer(5, TimeUnit.SECONDS)
                            .observeOn(Schedulers.io())
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                                emitter.onSuccess(location!!)
                            }, {
                                emitter.onError(NullPointerException())
                            })
                    )

                }, {
                    Log.d(TAG, "getHighAccuracyLocation-error:${it.localizedMessage}");
                    emitter.onError(it)
                })
            )
        }
    }


    fun getLocation(context: Context, bag: CompositeDisposable): MutableLiveData<Location> {
        val data = MutableLiveData<Location>()


        bag.add(
            getHighAccuracyLocation(context, bag)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    data.postValue(it)
                }, {
                    data.postValue(null)


                })
        )

        return data
    }
}
