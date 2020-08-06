package com.app.cameraxview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.cameraxview.DataPojo
import com.app.cameraxview.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

/**
 * Created by Rahul on 30/07/20.
 */
class CustomAdapter(val userList: ArrayList<DataPojo>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: DataPojo) {
            val latitude = itemView.findViewById(R.id.lat_id) as TextView
            val longitude = itemView.findViewById(R.id.lng_id) as TextView
            val accuracy = itemView.findViewById(R.id.accuracy_id) as TextView
            val barcode_id = itemView.findViewById(R.id.barcode_id) as TextView
            val Imagecapture = itemView.findViewById(R.id.ImageCapture) as ImageView
            latitude.text = "latitude ="+ user.latitude
            longitude.text ="longitude ="+ user.longtude
            accuracy.text ="accuracy ="+ user.accuracy
            barcode_id.text = user.barcode

            Glide.with(Imagecapture)
                .load(user.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(Imagecapture)
        }
    }
}