package com.app.cameraxview

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.cameraxview.mlkit.BarcodeActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_preview.*
import java.lang.reflect.Type


/**
 * Created by Rahul on 29/07/20.
 */

class PreviewActivity : AppCompatActivity(), QrCodeActivity.myBarcodInterface,
    MainActivity.getImageValue, BarcodeActivity.BarcodInterface {
    private val sharedPrefFile = "kotlinsharedpreference"
    private var editor: SharedPreferences.Editor? = null
    lateinit var imagePreview: ImageView
    var qrcodeValue: TextView? = null
    var qrcode_value: TextView? = null
    var sharedPreferences: SharedPreferences? = null
    var imageConstat: String = "ImageData"
    lateinit var cameraXView: Button
    lateinit var barcodeXview: Button
    lateinit var btn_qrrcode: Button
    var status: Boolean = false
    var imageurl: String? = null
    var BarCodeValue: String? = ""
    var newValue: String? = ""
    var locationValue: Location? = null

    companion object {
        var Filterlist: ArrayList<DataPojo>? = ArrayList()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        imagePreview = findViewById(R.id.image_preview)
        qrcodeValue = findViewById(R.id.qrcode_value)
        qrcode_value = findViewById(R.id.bar_code_value)
        cameraXView = findViewById(R.id.btn_camera)
        barcodeXview = findViewById(R.id.btn_barcode)
        btn_qrrcode = findViewById(R.id.btn_qrrcode)

        sharedPreferences = this.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences!!.edit()
        //val Qrcode=QrCodeActivity()
        QrCodeActivity.barcodeCallBack = this
        MainActivity.imageUrl = this
        BarcodeActivity.barcodeCallBack = this





        cameraXView.setOnClickListener(View.OnClickListener {

            startActivity(Intent(this@PreviewActivity, MainActivity::class.java))
//            val builder1 =
//                AlertDialog.Builder(this)
//            builder1.setMessage("To get Barcode value scan barcode first.")
//            builder1.setCancelable(true)
//
//            builder1.setPositiveButton(
//                "Yes"
//            ) { dialog, id ->
//
//            }
//
//            builder1.setNegativeButton(
//                "No"
//            ) { dialog, id -> dialog.cancel() }
//
//            val alert11 = builder1.create()
//            alert11.show()


        })
        barcodeXview.setOnClickListener(View.OnClickListener {

            startActivity(Intent(this, QrCodeActivity::class.java))
        })

        btn_qrrcode.setOnClickListener(View.OnClickListener {

            startActivity(
                Intent(
                    this,
                    BarcodeActivity::class.java
                )
            )
        })
        view_data_id.setOnClickListener(View.OnClickListener {

            startActivity(
                Intent(
                    this,
                    ViewDataActivity::class.java
                ).putExtra("data", Filterlist)
            )
        })



        getShareData()

    }

    override fun getQrcodeScane(values: String) {
        qrcodeValue!!.post { qrcodeValue!!.text = " QRCODE VALUE = " + values }

    }

    override fun getUrl(values: String, location: Location) {
        status = true
        Glide.with(imagePreview)
            .load(values)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imagePreview)
        Log.e("fndskmngdnfkgkfdg", values + " == " + location.accuracy)

        locationValue = location

        Log.e("gfhfghfghgfjghjjj", "" + qrcode_value!!.text.toString())


        Filterlist!!.add(
            DataPojo(
                values, location.latitude.toString(),
                location.longitude.toString(), location.accuracy.toString(), BarCodeValue!!
            )
        )
        val gson = Gson()
        val json: String = gson.toJson(Filterlist)
        editor!!.putString("Set", json)
        editor!!.commit()
        BarCodeValue = ""

        getShareData()
    }


    fun getShareData() {
        val gson = Gson()
        val json = sharedPreferences!!.getString("Set", "")
        if (json!!.isEmpty()) {
            // Toast.makeText(this, "There is something error", Toast.LENGTH_LONG).show()
        } else {
            val type: Type =
                object : TypeToken<List<DataPojo?>?>() {}.type
            val arrPackageData =
                gson.fromJson<List<DataPojo>>(json, type)



            Filterlist!!.clear()

            for (data in arrPackageData) {
                Filterlist!!.add(data)
                Log.e(
                    "kfjbdvjfbjvbfdbg",
                    "" + data + "  =======  " + data.latitude + " == " + data.longtude!! + " == " + data.accuracy
                )

            }
        }


    }

    override fun onResume() {
        super.onResume()

    }

    override fun getBarcodeScane(values: String) {
        Log.e("fndskmngdnfkgkfdg", values)
        if (values.equals("")) {
            qrcode_value!!.text = "NO BAR CODE VALUE"
            BarCodeValue = values
        } else {
            BarCodeValue = "BAR CODE VALUE =" + values

            qrcode_value!!.text = "BAR CODE VALUE =" + values

        }
    }
}