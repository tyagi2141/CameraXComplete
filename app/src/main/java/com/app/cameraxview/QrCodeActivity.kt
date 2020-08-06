package com.app.cameraxview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.QR_CODE
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


/**
 * Created by Rahul on 29/07/20.
 */
class QrCodeActivity :AppCompatActivity() {

    lateinit var cameraView:SurfaceView
     var barcodeInfo:TextView?=null
    lateinit var  cameraSource:CameraSource
     var barcodeString: String?=""

    companion object{
        var barcodeCallBack: myBarcodInterface? = null

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_main);

        cameraView = findViewById(R.id.suface_view);
        barcodeInfo = findViewById(R.id.code_info);
        barcodeInfo!!.text="no data"


        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(QR_CODE)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(640, 480)
            .build()

        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(cameraView.holder)
                } catch (ie: IOException) {
                    Log.e("CAMERA SOURCE", ie.toString())
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

                cameraSource.stop();
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {


            }
            override fun receiveDetections(detections: Detections<Barcode>) {

                val barcodes: SparseArray<Barcode> = detections.getDetectedItems()

                if (barcodes.size() != 0) {
                    barcodeInfo!!.post { barcodeInfo!!.text = barcodes.valueAt(0).displayValue }
                    barcodeString =barcodes.valueAt(0).displayValue
                    barcodeCallBack?.getQrcodeScane(barcodeString!!)
                    finish()

                }


            }
        })


    }


    interface myBarcodInterface {
        fun getQrcodeScane(values: String)
    }

}