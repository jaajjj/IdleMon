package com.example.idlemon

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.view.animation.LinearInterpolator

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null
    var soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).build()
    private val mapDeSon: HashMap<String, Int> = HashMap()
    private var compteurActivites = 0

    private const val FADE_DURATION = 800L

    private val playlistRoute = listOf(
        R.raw.route,
        R.raw.route1,
        R.raw.route2,
        R.raw.route3,
        R.raw.route4,
        R.raw.route5,
        R.raw.route6,
        R.raw.route7,
        R.raw.route8
    )
    private val playlistBoss = listOf(
        R.raw.boss,
        R.raw.boss1,
        R.raw.boss2,
        R.raw.boss3,
        R.raw.boss4,
        R.raw.boss5,
        R.raw.boss6,
        R.raw.boss7,
        R.raw.boss8,
        R.raw.boss9,
        R.raw.boss10
    )
    private val playlistHome = listOf(
        R.raw.home,
        R.raw.home2,
        R.raw.home3
    )
    private val playlistVoeux = listOf(
        R.raw.voeux
    )

    fun setup(context: Context) {
        if (mediaPlayer == null) {
            /*mapDeSon["son1"] = soundPool.load(context, R.raw.son1, 1)
            mapDeSon["son2"] = soundPool.load(context, R.raw.son2, 1)
            mapDeSon["son3"] = soundPool.load(context, R.raw.son3, 1)
            mapDeSon["faa"] = soundPool.load(context, R.raw.faa, 1)*/

            jouerPlaylistHome(context)
        }
    }

    fun lancerSequenceCombat(context: Context) {
        //pas de fadeOut
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) { e.printStackTrace() }

        try {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.battle1)
            mediaPlayer?.apply {
                isLooping = false
                setVolume(1.0f, 1.0f)
                start()

                setOnCompletionListener {
                    lancerBoucleCombatImmediate(context)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun lancerBoucleCombatImmediate(context: Context) {
        mediaPlayer?.release()
        val musiqueSuivante = playlistRoute.random()

        try {
            mediaPlayer = MediaPlayer.create(context.applicationContext, musiqueSuivante)
            mediaPlayer?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun jouerPlaylistHome(context: Context) {
        val musiqueSuivante = playlistHome.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }

    fun lancerVoeux(context: Context) {
        val musiqueSuivante = playlistVoeux.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }

    fun jouerPlaylistBoss(context: Context) {
        val musiqueSuivante = playlistBoss.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }
    fun jouerPlaylistBattle(context: Context) {
        val musiqueSuivante = playlistRoute.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }

    private fun transitionVersMusique(context: Context, resId: Int, loop: Boolean, onMusicStarted: (() -> Unit)? = null) {
        val currentPlayer = mediaPlayer

        if (currentPlayer != null && currentPlayer.isPlaying) {
            //fade Out
            val fadeOut = ValueAnimator.ofFloat(1.0f, 0.0f)
            fadeOut.duration = FADE_DURATION
            fadeOut.interpolator = LinearInterpolator()

            fadeOut.addUpdateListener { animation ->
                try {
                    val volume = animation.animatedValue as Float
                    currentPlayer.setVolume(volume, volume)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    try {
                        currentPlayer.stop()
                        currentPlayer.release()
                    } catch (e: Exception) { e.printStackTrace() }

                    lancerNouvelleMusiqueAvecFadeIn(context, resId, loop, onMusicStarted)
                }
            })
            fadeOut.start()
        } else {
            lancerNouvelleMusiqueAvecFadeIn(context, resId, loop, onMusicStarted)
        }
    }

    private fun lancerNouvelleMusiqueAvecFadeIn(context: Context, resId: Int, loop: Boolean, onMusicStarted: (() -> Unit)?) {
        try {
            mediaPlayer = MediaPlayer.create(context.applicationContext, resId)
            mediaPlayer?.apply {
                isLooping = loop
                setVolume(0.0f, 0.0f) //on commence silencieux
                start()
            }

            onMusicStarted?.invoke()

            //fade In
            val fadeIn = ValueAnimator.ofFloat(0.0f, 1.0f)
            fadeIn.duration = FADE_DURATION
            fadeIn.interpolator = LinearInterpolator()

            fadeIn.addUpdateListener { animation ->
                try {
                    val volume = animation.animatedValue as Float
                    mediaPlayer?.setVolume(volume, volume)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            fadeIn.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.setVolume(0.1f, 0.1f)
            mediaPlayer?.start()
            val resumeFade = ValueAnimator.ofFloat(0.1f, 1.0f)
            resumeFade.duration = 300
            resumeFade.addUpdateListener { anim ->
                val v = anim.animatedValue as Float
                mediaPlayer?.setVolume(v, v)
            }
            resumeFade.start()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
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
            play()
        }
        compteurActivites++
    }

    fun onStopActivity() {
        compteurActivites--
        if (compteurActivites == 0) {
            pause()
        }
    }
}