package com.example.idlemon

import android.content.Context

object SettingsManager {
    private const val PREFS_NAME = "MesParametres"
    private const val KEY_MUSIC = "music_enabled"
    private const val KEY_FAST_DIALOGUE = "fast_dialogue"

    fun isMusicEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MUSIC, true)
    }

    fun setMusicEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply()
    }

    fun isFastDialogue(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FAST_DIALOGUE, false) //Désactivé par défaut
    }

    fun setFastDialogue(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FAST_DIALOGUE, enabled).apply()
    }
}