package com.k2.zoomimageview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ZoomImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.iv)
        imageView.debugInfoVisible = true
        imageView.onDrawableLoaded = {
            findViewById<View>(R.id.progress).visibility = View.GONE
        }
        imageView.load("https://images.unsplash.com/photo-1534142499731-a32a99935397?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=2734&q=80")
    }
}