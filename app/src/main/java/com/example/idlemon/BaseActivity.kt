package com.example.idlemon

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        MusicManager.onStartActivity()
    }

    override fun onStop() {
        super.onStop()
        MusicManager.onStopActivity()
    }
}