package com.example.idlemon

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool


class MusicManager(pContext: MainActivity) {


    lateinit var mediaPlayer: MediaPlayer
    var soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).build()
    private val mapDeSon: HashMap<String, Int> = HashMap()
    public val context = pContext


    init {
        /*
        mapDeSon["son1"] = soundPool.load(context, R.raw.son1, 1)
        mapDeSon["son2"] = soundPool.load(context, R.raw.son2, 1)
        mapDeSon["son3"] = soundPool.load(context, R.raw.son3, 1)
        mapDeSon["faa"] = soundPool.load(context, R.raw.faa, 1)

        mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.superidol)
        mediaPlayer.setVolume(1f, 1f)
        mediaPlayer.isLooping = true
        mediaPlayer.start()*/
    }


    fun stop() { mediaPlayer.stop() }
    fun pause() { mediaPlayer.pause() }
    fun resume() { mediaPlayer.start() }


    fun playNotif(soundKey: String = "faa") {
        mapDeSon[soundKey]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
}
