package com.app.cameraxview

import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * Created by Rahul on 30/07/20.
 */

@Parcelize
class DataPojo(

    var imageUrl : String?=null,
    var latitude:String?=null,
    var longtude:String?=null,
    var accuracy:String?=null,
    var barcode : String?="NA",
    var QrCode : String?="NA"
    ):Parcelable {
    override fun toString(): String {
        return "DataPojo(imageUrl=$imageUrl, latitude=$latitude, longtude=$longtude, accuracy=$accuracy, barcode=$barcode, QrCode=$QrCode)"
    }
}