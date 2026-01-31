package com.example.idlemon

object DataManager {
    lateinit var model: ModelJson

    fun setup(context: android.content.Context) {
        model = ModelJson(context)
    }
}