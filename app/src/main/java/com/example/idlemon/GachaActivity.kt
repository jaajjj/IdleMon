package com.example.idlemon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class GachaActivity : BaseActivity() {

    //UI
    private lateinit var homeBtn: ImageView
    private lateinit var teamBtn: ImageView
    private lateinit var singlePullBtn: LinearLayout
    private lateinit var settingsBtn: ImageView
    private lateinit var tenPullBtn: LinearLayout
    private lateinit var fieldPokegold: TextView

    //UI Vidéo
    private lateinit var videoContainer: FrameLayout
    private lateinit var summonVideoView: VideoView
    private lateinit var skipBtn: TextView

    private var calculationThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gacha)

        //init les vues
        initViews()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.hide(android.view.WindowInsets.Type.systemBars())
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //affiche les gold
        refreshUI()

        teamBtn.setOnClickListener { startActivity(Intent(this, TeamActivity::class.java)) }
        homeBtn.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        settingsBtn.setOnClickListener {
            showSettingsDialog()
        }
        
        singlePullBtn.setOnClickListener {
            if (Player.getPieces() >= 100) {
                playSummonAnimation(1)
                lancerCalculEnArrierePlan(1)
            }
        }

        tenPullBtn.setOnClickListener {
            if (Player.getPieces() >= 1000) {
                playSummonAnimation(10)
                lancerCalculEnArrierePlan(10)
            }
        }
    }

    private fun initViews() {
        homeBtn = findViewById(R.id.homeBtn)
        teamBtn = findViewById(R.id.teamBtn)
        singlePullBtn = findViewById(R.id.singlePullBtn)
        tenPullBtn = findViewById(R.id.tenPullBtn)
        fieldPokegold = findViewById(R.id.fieldPokegold)
        settingsBtn = findViewById(R.id.settingsBtn)
        videoContainer = findViewById(R.id.videoContainer)
        summonVideoView = findViewById(R.id.summonVideoView)
        skipBtn = findViewById(R.id.skipBtn)
    }

    //refresh global de l'UI
    override fun refreshUI() {
        super.refreshUI()
        if (::fieldPokegold.isInitialized) {
            fieldPokegold.text = Player.getPieces().toString()
        }
    }

    override fun onResume() {
        super.onResume()
        videoContainer.visibility = View.GONE
        //refresh au retour sur la page
        refreshUI()
    }

    private fun lancerCalculEnArrierePlan(nbVoeux: Int) {
        calculationThread = Thread {
            val list = mutableListOf<Pair<List<Pokemon>, Int>>()
            val eggCount = if (nbVoeux == 10) 7 else 5

            for (i in 0 until eggCount) {
                val pokemonsInEgg = if (nbVoeux == 10) {
                    List(10) { DataManager.model.getRandomPokemon() }
                } else {
                    listOf(DataManager.model.getRandomPokemon())
                }

                val bestPokemon = pokemonsInEgg.maxByOrNull { getRarityScore(it.species.rarete) }
                    ?: pokemonsInEgg.first()

                val drawableRes = when(bestPokemon.species.rarete) {
                    "Legendaire" -> R.drawable.egg_leg
                    "Fabuleux" -> R.drawable.egg_fab
                    "Epique" -> R.drawable.egg_epique
                    else -> R.drawable.egg
                }

                list.add(Pair(pokemonsInEgg, drawableRes))
            }
            GachaSession.preparedEggs = list
        }
        calculationThread?.start()
    }

    private fun getRarityScore(rarete: String): Int {
        return when (rarete) {
            "Legendaire" -> 5
            "Fabuleux" -> 4
            "Epique" -> 3
            "Rare" -> 2
            else -> 1
        }
    }

    private fun playSummonAnimation(nbVoeux: Int) {
        var isFinished = false
        MusicManager.stop()
        videoContainer.visibility = View.VISIBLE
        val videoPath = "android.resource://" + packageName + "/" + R.raw.anim_gacha
        val uri = Uri.parse(videoPath)
        summonVideoView.setVideoURI(uri)

        summonVideoView.setOnCompletionListener {
            if (!isFinished) {
                isFinished = true
                goToResultActivity(nbVoeux)
            }
        }
        skipBtn.setOnClickListener {
            if (!isFinished) {
                isFinished = true
                summonVideoView.stopPlayback()
                goToResultActivity(nbVoeux)
            }
        }

        summonVideoView.start()
    }

    private fun goToResultActivity(nb: Int) {
        try {
            calculationThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val intent = if (nb == 1) {
            Intent(this, SinglePullActivity::class.java)
        } else {
            Intent(this, TenPullActivity::class.java)
        }
        MusicManager.lancerVoeux(this)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
