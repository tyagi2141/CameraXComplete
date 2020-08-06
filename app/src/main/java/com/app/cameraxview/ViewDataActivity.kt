package com.app.cameraxview

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.cameraxview.PreviewActivity.Companion.Filterlist
import com.app.cameraxview.adapter.CustomAdapter
import kotlinx.android.synthetic.main.viewdata_main.*

/**
 * Created by Rahul on 30/07/20.
 */
class ViewDataActivity : AppCompatActivity() {

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewdata_main)

        //getting recyclerview from xml
      //  val recyclerView:RecyclerView = findViewById(R.id.recyclerView)

        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        //crating an arraylist to store users using the data class user


        //creating our adapter
        val adapter = CustomAdapter(Filterlist!!)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter
    }
}