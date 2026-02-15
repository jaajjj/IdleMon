package com.example.idlemon

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView

interface PanoramaUI {
    val context: Context
    val backgroundImage: ImageView
    val eggsContainer: FrameLayout
    val boussole: ImageView
}