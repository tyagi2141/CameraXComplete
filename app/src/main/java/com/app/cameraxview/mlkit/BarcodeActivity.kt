package com.app.cameraxview.mlkit

import android.content.pm.ActivityInfo
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.cameraxview.QrCodeActivity
import com.app.cameraxview.R
import com.app.cameraxview.mlkit.CameraView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

/**
 * Created by Rahul on 29/07/20.
 */
class BarcodeActivity : AppCompatActivity() {
    private var mCamera: Camera? = null
    private var camView: CameraView? = null
    private var overlay: OverlayView? = null
    private var overlayScale = -1.0
    var oldValue: String? = null
    var newValue: String? = ""
    var barcode_value: TextView? = null

    var barcodData: String? = ""


    private interface OnBarcodeListener {
        fun onIsbnDetected(barcode: FirebaseVisionBarcode?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Full Screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Fix orientation : portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Set layout
        setContentView(R.layout.camera_activity_preview)
        barcode_value = findViewById(R.id.barcode_value)
        findViewById<View>(R.id.btn_finish_preview).setOnClickListener {
            barcode_value!!.text=""
            finish()
        }
        // Initialize Camera
        mCamera = cameraInstance

        // Set-up preview screen
        if (mCamera != null) {
            // Create overlay view
            overlay = OverlayView(this)

            // Create barcode processor for ISBN
            val camCallback = CustomPreviewCallback(
                CameraView.PREVIEW_WIDTH,
                CameraView.PREVIEW_HEIGHT
            )
            camCallback.setBarcodeDetectedListener(object : OnBarcodeListener {
                override fun onIsbnDetected(barcode: FirebaseVisionBarcode?) {
                    overlay!!.setOverlay(
                        fitOverlayRect(barcode!!.boundingBox),
                        barcode.rawValue
                    )
                    overlay!!.invalidate()
                    barcodData = barcode.rawValue
                    Log.e("QrcodeValue", "" + barcodData)
                }
            })

            // Create camera preview
            camView = CameraView(this, mCamera)
            camView!!.setPreviewCallback(camCallback)

            // Add view to UI
            val preview = findViewById<FrameLayout>(R.id.frm_preview)
            preview.addView(camView)
            preview.addView(overlay)
        }
    }

    override fun onDestroy() {
        try {
            if (mCamera != null) mCamera!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    /**
     * Calculate overlay scale factor
     */
    private fun fitOverlayRect(r: Rect?): Rect {
        if (overlayScale <= 0) {
            val prevSize = camView!!.getPreviewSize()
            overlayScale = overlay!!.width.toDouble() / prevSize!!.height.toDouble()
        }
        return Rect(
            (r!!.left * overlayScale).toInt(),
            (r.top * overlayScale).toInt(),
            (r.right * overlayScale).toInt(),
            (r.bottom * overlayScale).toInt()
        )
    }

    /**
     * Post-processor for preview image streams
     */
    private inner class CustomPreviewCallback internal constructor(
        /**
         * size of input image
         */
        private val mImageWidth: Int,
        private val mImageHeight: Int
    ) :
        PreviewCallback, OnSuccessListener<List<FirebaseVisionBarcode>>,
        OnFailureListener {
        fun setBarcodeDetectedListener(mBarcodeDetectedListener: OnBarcodeListener?) {
            this.mBarcodeDetectedListener = mBarcodeDetectedListener
        }

        // ML Kit instances
        private val options: FirebaseVisionBarcodeDetectorOptions
        private val detector: FirebaseVisionBarcodeDetector
        private val metadata: FirebaseVisionImageMetadata

        /**
         * Event Listener for post processing
         *
         *
         * We'll set up the detector only for EAN-13 barcode format and ISBN barcode type.
         * This OnBarcodeListener aims of notifying 'ISBN barcode is detected' to other class.
         */
        private var mBarcodeDetectedListener: OnBarcodeListener? = null

        /**
         * Start detector if camera preview shows
         */
        override fun onPreviewFrame(
            data: ByteArray,
            camera: Camera
        ) {
            try {
                detector.detectInImage(FirebaseVisionImage.fromByteArray(data, metadata))
                    .addOnSuccessListener(this)
                    .addOnFailureListener(this)
            } catch (e: Exception) {
                Log.d("CameraView", "parse error")
            }
        }

        /**
         * Barcode is detected successfully
         */
        override fun onSuccess(barcodes: List<FirebaseVisionBarcode>) {
            // Task completed successfully
            for (barcode in barcodes) {
                Log.d("Barcode", "value : " + barcode.rawValue)
                // barcodData = barcode.getRawValue();
                newValue = barcode.rawValue
                val valueType = barcode.valueType
                if (valueType == FirebaseVisionBarcode.TYPE_ISBN) {
                    mBarcodeDetectedListener!!.onIsbnDetected(barcode)
                    //  barcodData = barcode.getRawValue();
                    return
                }
            }
            if (newValue != oldValue) {
                oldValue = newValue
            } else {
                barcodData = newValue
            }
            if (barcodData != "") {
                barcode_value!!.text = barcodData
                // finish();

                barcodeCallBack?.getBarcodeScane(barcodData!!)
            }

            //            new QrCodeInterface() {
//                @Overrideu
//                public String getBarcodecodeScane() {
//                    return barcodData;
//                }
//            };
        }

        /**
         * Barcode is not recognized
         */
        override fun onFailure(e: Exception) {
            // Task failed with an exception
            Log.i("Barcode", "read fail")
        }

        /**
         * Constructor
         *
         * @param imageWidth  preview image width (px)
         * @param imageHeight preview image height (px)
         */
        init {

            // set-up detector options for find EAN-13 format (commonly used 1-D barcode)
            options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13)
                .build()
            detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

            // build detector
            metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(ImageFormat.NV21)
                .setWidth(mImageWidth)
                .setHeight(mImageHeight)
                .setRotation(FirebaseVisionImageMetadata.ROTATION_90)
                .build()
        }
    }

    companion object {
        var barcodeCallBack: BarcodInterface? = null

        /**
         * Get facing back camera instance
         */
        val cameraInstance: Camera?
            get() {
                var camId = -1
                val cameraInfo =
                    Camera.CameraInfo()
                for (i in 0 until Camera.getNumberOfCameras()) {
                    Camera.getCameraInfo(i, cameraInfo)
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        camId = i
                        break
                    }
                }
                if (camId == -1) return null
                var c: Camera? = null
                try {
                    c = Camera.open(camId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return c
            }
    }

    interface BarcodInterface {
        fun getBarcodeScane(values: String)
    }
}