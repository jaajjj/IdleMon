package com.example.idlemon

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null
    var soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).build()
    private val mapDeSon: HashMap<String, Int> = HashMap()
    private var compteurActivites = 0

    // Ajoute ici tes autres musiques pour la boucle après l'intro
    private val playlist = listOf(
        R.raw.battle1
    )

    fun setup(context: Context) {
        if (mediaPlayer == null) {
            /*mapDeSon["son1"] = soundPool.load(context, R.raw.son1, 1)
            mapDeSon["son2"] = soundPool.load(context, R.raw.son2, 1)
            mapDeSon["son3"] = soundPool.load(context, R.raw.son3, 1)
            mapDeSon["faa"] = soundPool.load(context, R.raw.faa, 1)*/

            // Par défaut on lance battle1 en boucle (pour le menu)
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.battle1)
            mediaPlayer?.setVolume(1f, 1f)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    fun lancerSequenceCombat(context: Context) {
        //on coupe la musique
        mediaPlayer?.stop()
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.battle1)
        mediaPlayer?.isLooping = false
        mediaPlayer?.start()

        //playlist
        mediaPlayer?.setOnCompletionListener {
            jouerPlaylist(context)
        }
    }

    private fun jouerPlaylist(context: Context) {
        // Si ta playlist est vide, on ne fait rien (ou on remet battle1)
        if (playlist.isEmpty()) return

        mediaPlayer?.release()

        // Prend une musique au hasard (ou la première [0])
        val musiqueSuivante = playlist.random()

        mediaPlayer = MediaPlayer.create(context.applicationContext, musiqueSuivante)
        mediaPlayer?.isLooping = true // Celle-ci boucle à l'infini
        mediaPlayer?.start()
    }

    fun play() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
        mediaPlayer?.seekTo(0)
    }

    fun resume() {
        play()
    }

    fun playNotif(soundKey: String = "faa") {
        mapDeSon[soundKey]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun onStartActivity() {
        if (compteurActivites == 0) {
            play() // On relance si on revient dans l'appli
        }
        compteurActivites++
    }

    fun onStopActivity() {
        compteurActivites--
        if (compteurActivites == 0) {
            pause() // On coupe si on quitte l'appli
        }
    }
}