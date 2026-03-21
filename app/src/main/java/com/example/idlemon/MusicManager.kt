package com.example.idlemon

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.view.animation.LinearInterpolator
import android.widget.Toast

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null
    var soundPool: SoundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val mapDePokeCri: HashMap<String, Int> = HashMap()
    private val mapDeBossCri: HashMap<String, Int> = HashMap()
    private val mapDeSonBattle: HashMap<String, Int> = HashMap()
    private var dernierCriJoue: String = ""
    private val mapDeMoveSounds: HashMap<String, MutableList<Int>> = HashMap()
    private val mapDeNomsDesSons: HashMap<Int, String> = HashMap()
    private var appContext: Context? = null

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

    //sons attacks
    private val sonsAttackFeu = listOf(
        R.raw.feu_move1,
        R.raw.feu_move3,
        R.raw.feu_move4,
        R.raw.feu_move5,
        R.raw.feu_move6,
        R.raw.feu_move7
    )

    private val sonsAttackEau = listOf(
        R.raw.eau_move1,
        R.raw.eau_move2,
        R.raw.eau_move3
    )

    private val sonsAttackPlante = listOf(
        R.raw.plante_move1,
        R.raw.plante_move2,
        R.raw.plante_move3
    )

    private val sonsAttackElec = listOf(
        R.raw.elec_move1,
        R.raw.elec_move2,
        R.raw.elec_move3,
        R.raw.elec_move4,
        R.raw.elec_move5
    )

    private val sonsAttackNormal = listOf(
        R.raw.normal_move1,
        R.raw.normal_move2,
        R.raw.normal_move3,
        R.raw.normal_move4
    )

    private val sonsAttackFee = listOf(
        R.raw.fee_move1,
        R.raw.fee_move2,
        R.raw.fee_move3
    )

    private val sonsAttackPsy = listOf(
        R.raw.psy_move1,
        R.raw.psy_move2,
        R.raw.psy_move3
    )

    private val sonsAttackGlace = listOf(
        R.raw.glace_move1,
        R.raw.glace_move2
    )

    private val sonsAttackSpectre = listOf(
        R.raw.spectre_move1,
        R.raw.spectre_move2,
        R.raw.spectre_move3
    )

    private val sonsAttackTenebre = listOf(
        R.raw.tenebre_move1,
        R.raw.tenebre_move2
    )

    private val sonsAttackDragon = listOf(
        R.raw.drag_move1,
        R.raw.drag_move2,
        R.raw.drag_move3,
        R.raw.drag_move4
    )

    private val sonsAttackVol = listOf(
        R.raw.air_move1,
        R.raw.air_move2,
        R.raw.air_move3,
        R.raw.air_move4
    )

    private val sonsAttackSol = listOf(
        R.raw.sol_move1,
        R.raw.sol_move2)

    private val sonsAttackRoche = listOf(
        R.raw.roche_move1,
        R.raw.roche_move2
    )

    private val sonsAttackAcier = listOf(
        R.raw.acier_move1,
        R.raw.acier_move2
    )

    private val sonsAttackInsect = listOf(
        R.raw.insect_move1,
        R.raw.insect_move2,
        R.raw.insect_move3,
        R.raw.insect_move4,
        R.raw.insect_move5
    )

    private val sonsAttackMalus = listOf(
        R.raw.malus_move1,
        R.raw.malus_move2,
        R.raw.malus_move3
    )

    private val sonsAttackBonus = listOf(
        R.raw.bonus_move1,
        R.raw.bonus_move2,
        R.raw.bonus_move3,
        R.raw.bonus_move4
    )

    private val sonsAttackPoison = listOf(
        R.raw.poison_move1,
        R.raw.poison_move2,
        R.raw.poison_move3
    )

    private val sonsAttackCombat = listOf(
        R.raw.combat_move1,
        R.raw.combat_move2,
        R.raw.combat_move3
    )

    fun setup(context: Context) {
        appContext = context.applicationContext
        if (mediaPlayer == null) {

            //load des pokeCri
            for(i in 1..44) { mapDePokeCri["pokeCri$i"] = soundPool.load(context, context.resources.getIdentifier("poke_cri$i", "raw", context.packageName), 1) }
            mapDePokeCri["zacian"] = soundPool.load(context, R.raw.zacian, 1)
            mapDePokeCri["pikachu"] = soundPool.load(context, R.raw.pikachu, 1)

            //load des BossCri
            for(i in 1..45){ mapDeBossCri["bossCri$i"] = soundPool.load(context, context.resources.getIdentifier("boss_cri$i", "raw", context.packageName), 1) }

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
            mapDeSonBattle["levelUp_sound"] = soundPool.load(context, R.raw.level_up, 1)
            mapDeSonBattle["evo_sound"] = soundPool.load(context, R.raw.evo_sound, 1)


            val drainId = context.resources.getIdentifier("drain_move", "raw", context.packageName)
            mapDeMoveSounds["drain"] = mutableListOf(soundPool.load(context, drainId, 1))
            mapDeNomsDesSons[mapDeMoveSounds["drain"]!![0]] = "drain_move"

            val toutesLesAttaques = mapOf(
                "feu" to sonsAttackFeu, "eau" to sonsAttackEau, "plante" to sonsAttackPlante,
                "electrik" to sonsAttackElec, "normal" to sonsAttackNormal, "fee" to sonsAttackFee,
                "psy" to sonsAttackPsy, "glace" to sonsAttackGlace, "spectre" to sonsAttackSpectre,
                "tenebre" to sonsAttackTenebre, "dragon" to sonsAttackDragon, "vol" to sonsAttackVol,
                "sol" to sonsAttackSol, "roche" to sonsAttackRoche, "acier" to sonsAttackAcier,
                "insect" to sonsAttackInsect, "malus" to sonsAttackMalus, "bonus" to sonsAttackBonus,
                "poison" to sonsAttackPoison, "combat" to sonsAttackCombat
            )

            for ((type, listRaw) in toutesLesAttaques) {
                val listeIdsLoaded = mutableListOf<Int>()
                for (rawId in listRaw) {
                    val idSoundPool = soundPool.load(context, rawId, 1)
                    listeIdsLoaded.add(idSoundPool)
                    mapDeNomsDesSons[idSoundPool] = context.resources.getResourceEntryName(rawId)
                }
                mapDeMoveSounds[type] = listeIdsLoaded
            }

            jouerPlaylistHome(context)
        }
    }

    fun lancerSequenceCombat(context: Context) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) { e.printStackTrace() }

        try {
            currentResId = R.raw.battle1 // <-- AJOUT ICI
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
        currentResId = musiqueSuivante // <-- AJOUT ICI
        mediaPlayer = MediaPlayer.create(context.applicationContext, musiqueSuivante)
        mediaPlayer?.apply {
            isLooping = true
            setVolume(1.0f, 1.0f)
            start()
        }
    }

    private fun lancerNouvelleMusiqueAvecFadeIn(context: Context, resId: Int, loop: Boolean, onMusicStarted: (() -> Unit)?) {
        try {
            currentResId = resId // <-- AJOUT ICI
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

    fun jouerPlaylistHome(context: Context) {
        if (currentResId == R.raw.home && mediaPlayer?.isPlaying == true) return
        stop()
        currentResId = R.raw.home
        mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.home)
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.start()
    }

    fun lancerVoeux(context: Context) {
        val musiqueSuivante = playlistVoeux.random()
        transitionVersMusique(context, musiqueSuivante, loop = true)
    }

    //BATTLE
    fun jouerSonAttaque(attaque: Attack) {
        val typeClair = attaque.type.lowercase()
        val ctx = appContext ?: return

        val listeSons: List<Int>?

        //0 dmg
        if (attaque.basePower == 0) {
            val isBonusOrHeal = !attaque.bonus.isNullOrEmpty() || attaque.heal > 0
            listeSons = if (isBonusOrHeal) {
                mapDeMoveSounds["bonus"]
            } else {
                mapDeMoveSounds["malus"]
            }
        } else {
            //attack de dégat
            listeSons = mapDeMoveSounds[typeClair] ?: mapDeMoveSounds["normal"]
        }

        if (!listeSons.isNullOrEmpty()) {
            val sonChoisiId = listeSons.random()
            soundPool.play(sonChoisiId, 0.5f, 0.5f, 1, 0, 1f)
        }
    }

    fun jouerSonBattle(nomSon: String) {
        mapDeSonBattle[nomSon]?.let { soundId ->
            soundPool.play(soundId, 0.5f, 0.5f, 1, 0, 1f)
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

    fun crierPokemon(pok: Pokemon) {
        //si zacian ou pikachi, son custop, sinon random
        if (pok.species.nom == "Zacian") {
            mapDePokeCri["zacian"]?.let { soundPool.play(it, 0.6f, 0.6f, 1, 0, 1f) }
            return
        }
        if (pok.species.nom == "Pikachu") {
            mapDePokeCri["pikachu"]?.let { soundPool.play(it, 0.6f, 0.6f, 1, 0, 1f) }
            return
        }
        val clesDispo = mapDePokeCri.keys.filter { it != "zacian" && it != "pikachu" && it != dernierCriJoue }
        if (clesDispo.isNotEmpty()) {
            val soundKey = clesDispo.random()
            dernierCriJoue = soundKey // On mémorise ce cri
            mapDePokeCri[soundKey]?.let { soundId ->
                soundPool.play(soundId, 0.6f, 0.6f, 1, 0, 1f)
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

    fun sonLevelUpPoke() {
        mapDeSonBattle["levelUp_sound"]?.let { soundId ->
            soundPool.play(soundId, 0.4f, 0.4f, 1, 0, 1f)
        }
    }
    fun sonEvoPoke() {
        mapDeSonBattle["evo_sound"]?.let { soundId ->
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

    //debug :
    /*fun lancerDebugSonsAttaques(context: Context) {
        stop()
        val toutesLesAttaquesRaw = sonsAttackFeu + sonsAttackEau + sonsAttackPlante +
                sonsAttackElec + sonsAttackNormal + sonsAttackFee + sonsAttackPsy +
                sonsAttackGlace + sonsAttackSpectre + sonsAttackTenebre + sonsAttackDragon +
                sonsAttackVol + sonsAttackSol + sonsAttackRoche + sonsAttackAcier +
                sonsAttackInsect + sonsAttackMalus + sonsAttackBonus + sonsAttackPoison +
                sonsAttackCombat
        Toast.makeText(context, "Début du debug : ${toutesLesAttaquesRaw.size} sons à tester", Toast.LENGTH_SHORT).show()
        //lecture rec
        playNextDebugSound(context, toutesLesAttaquesRaw.iterator())
    }

    private fun playNextDebugSound(context: Context, iterator: Iterator<Int>) {
        if (!iterator.hasNext()) {
            Toast.makeText(context, "Fin du test des sons !", Toast.LENGTH_SHORT).show()
            return
        }
        val rawId = iterator.next()
        val nomDuSon = context.resources.getResourceEntryName(rawId)
        Toast.makeText(context, "Son : $nomDuSon", Toast.LENGTH_SHORT).show()
        val soundId = mapDeNomsDesSons.entries.find { it.value == nomDuSon }?.key

        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            playNextDebugSound(context, iterator)
        }, 4000)
    }*/
}