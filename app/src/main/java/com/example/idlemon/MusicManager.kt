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
    var soundPool: SoundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val mapDePokeCri: HashMap<String, Int> = HashMap()
    private val mapDeBossCri: HashMap<String, Int> = HashMap()
    private val mapDeSonBattle: HashMap<String, Int> = HashMap()
    private var dernierCriJoue: String = ""
    private val mapDeMoveSounds: HashMap<String, MutableList<Int>> = HashMap()

    private var compteurActivites = 0

    private const val FADE_DURATION = 800L
    private var currentResId: Int? = null


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
        R.raw.home
    )
    private val playlistVoeux = listOf(
        R.raw.voeux
    )

    fun setup(context: Context) {
        if (mediaPlayer == null) {
            //load des pokeCri
            for(i in 1..44) {
                mapDePokeCri["pokeCri$i"] = soundPool.load(context, context.resources.getIdentifier("poke_cri$i", "raw", context.packageName), 1)
            }
            mapDePokeCri["zacian"] = soundPool.load(context, R.raw.zacian, 1)
            mapDePokeCri["pikachu"] = soundPool.load(context, R.raw.pikachu, 1)

            //load des BossCri
            for(i in 1..45){
                mapDeBossCri["bossCri$i"] = soundPool.load(context, context.resources.getIdentifier("boss_cri$i", "raw", context.packageName), 1)
            }
            //load des sons
            mapDeSonBattle["poke_spawn"] = soundPool.load(context, R.raw.poke_spawn, 1)
            mapDeSonBattle["ko_sound"] = soundPool.load(context, R.raw.ko_sound, 1)
            mapDeSonBattle["heal"] = soundPool.load(context, R.raw.heal, 1)
            mapDeSonBattle["item_active"] = soundPool.load(context, R.raw.item_active, 1)
            mapDeSonBattle["weak_eff"] = soundPool.load(context, R.raw.weak_eff, 1)
            mapDeSonBattle["super_eff"] = soundPool.load(context, R.raw.super_eff, 1)
            mapDeSonBattle["low_hp"] = soundPool.load(context, R.raw.low_hp, 1)
            mapDeSonBattle["malus_stat_sound"] = soundPool.load(context, R.raw.malus_stat_sound, 1)
            mapDeSonBattle["bonus_stat_sound"] = soundPool.load(context, R.raw.bonus_stat_sound, 1)

            //Types moves sons
            val moveCounts = mapOf(
                "normal" to 4, "combat" to 3, "air" to 4, "poison" to 3,
                "feu" to 7, "eau" to 3, "plante" to 3, "elec" to 5,
                "glace" to 2, "sol" to 3, "roche" to 2, "acier" to 2,
                "psy" to 3, "spectre" to 3, "tenebre" to 2, "fee" to 3,
                "drag" to 4, "insect" to 5, "bonus" to 4, "malus" to 3
            )
            for ((type, count) in moveCounts) {
                val soundIds = mutableListOf<Int>()
                for (i in 1..count) {
                    val resName = "${type}_move$i" // ex: feu_move1, eau_move2...
                    val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
                    soundIds.add(soundPool.load(context, resId, 1))
                }
                mapDeMoveSounds[type] = soundIds
            }
            val drainId = context.resources.getIdentifier("drain_move", "raw", context.packageName)
            mapDeMoveSounds["drain"] = mutableListOf(soundPool.load(context, drainId, 1))

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
        mediaPlayer = MediaPlayer.create(context.applicationContext, musiqueSuivante)
        mediaPlayer?.apply {
            isLooping = true
            setVolume(1.0f, 1.0f)
            start()
        }
    }

    fun jouerPlaylistHome(context: Context) {
        if (currentResId != null && playlistHome.contains(currentResId) && mediaPlayer?.isPlaying == true) {
            return
        }

        val musiqueSuivante = playlistHome.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }

    fun lancerVoeux(context: Context) {
        val musiqueSuivante = playlistVoeux.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }

    //BATTLE
    fun jouerSonAttaque(type: String) {
        val typeKey = type.lowercase()

        val listeSons = mapDeMoveSounds[typeKey]

        if (!listeSons.isNullOrEmpty()) {
            val soundId = listeSons.random()
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            mapDeMoveSounds["normal"]?.randomOrNull()?.let {
                soundPool.play(it, 1f, 1f, 1, 0, 1f)
            }
        }
    }
    fun jouerSonBattle(nomSon: String) {
        mapDeSonBattle[nomSon]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
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

    /*fun playNotif(soundKey: String = "faa") {
        mapDeSon[soundKey]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }*/

    fun crierPokemon(pok: Pokemon) {
        //si zacian ou pikachi, son custop, sinon random
        if (pok.species.nom == "Zacian") {
            mapDePokeCri["zacian"]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
            return
        }
        if (pok.species.nom == "Pikachu") {
            mapDePokeCri["pikachu"]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
            return
        }
        val clesDispo = mapDePokeCri.keys.filter { it != "zacian" && it != "pikachu" && it != dernierCriJoue }
        if (clesDispo.isNotEmpty()) {
            val soundKey = clesDispo.random()
            dernierCriJoue = soundKey // On mémorise ce cri
            mapDePokeCri[soundKey]?.let { soundId ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    fun crierBoss() {
        mapDeBossCri.keys.random().let { soundKey ->
            mapDeBossCri[soundKey]?.let { soundId ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }
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