package com.example.idlemon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GachaActivity : AppCompatActivity() {

    // UI
    private lateinit var homeBtn: ImageView
    private lateinit var teamBtn: ImageView
    private lateinit var singlePullBtn: Button
    private lateinit var tenPullBtn: Button
    private lateinit var fieldPokegold: TextView

    // UI Vidéo
    private lateinit var videoContainer: FrameLayout
    private lateinit var summonVideoView: VideoView
    private lateinit var skipBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gacha)

        // Gestion plein écran (Barres système)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Init vues classiques
        homeBtn = findViewById(R.id.homeBtn)
        teamBtn = findViewById(R.id.teamBtn)
        singlePullBtn = findViewById(R.id.singlePullBtn)
        tenPullBtn = findViewById(R.id.tenPullBtn)
        fieldPokegold = findViewById(R.id.fieldPokegold)

        // Init vues Vidéo
        videoContainer = findViewById(R.id.videoContainer)
        summonVideoView = findViewById(R.id.summonVideoView)
        skipBtn = findViewById(R.id.skipBtn)

        val player = Player
        fieldPokegold.text = player.getPieces().toString()

        // Navigation Footer
        teamBtn.setOnClickListener { startActivity(Intent(this, TeamActivity::class.java)) }
        homeBtn.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        // --- Clic sur 1 Voeu ---
        singlePullBtn.setOnClickListener {
            if (player.getPieces() >= 100) {
                player.setPieces(player.getPieces() - 100)
                fieldPokegold.text = player.getPieces().toString()

                // Lancer la vidéo pour 1 tirage
                playSummonAnimation(1)
            }
        }

        // --- Clic sur 10 Voeux ---
        tenPullBtn.setOnClickListener {
            if (player.getPieces() >= 1000) {
                player.setPieces(player.getPieces() - 1000)
                fieldPokegold.text = player.getPieces().toString()

                // Lancer la vidéo pour 10 tirages
                playSummonAnimation(10)
            }
        }
    }

    private fun playSummonAnimation(nbVoeux: Int) {
        // 1. Afficher le container vidéo (fond noir)
        videoContainer.visibility = View.VISIBLE

        // 2. Préparer le chemin de la vidéo (res/raw/anim_gacha.mp4)
        // Assure-toi que la vidéo s'appelle bien "anim_gacha" dans res/raw
        val videoPath = "android.resource://" + packageName + "/" + R.raw.anim_gacha
        val uri = Uri.parse(videoPath)
        summonVideoView.setVideoURI(uri)

        // 3. Définir ce qui se passe quand la vidéo est finie
        summonVideoView.setOnCompletionListener {
            goToResultActivity(nbVoeux)
        }

        // 4. Bouton Passer (arrête la vidéo et va direct au résultat)
        skipBtn.setOnClickListener {
            summonVideoView.stopPlayback()
            goToResultActivity(nbVoeux)
        }

        // 5. Action !
        summonVideoView.start()
    }

    private fun goToResultActivity(nb: Int) {
        // On lance l'activité suivante
        val intent = if (nb == 1) {
            Intent(this, SinglePullActivity::class.java)
        } else {
            Intent(this, TenPullActivity::class.java)
        }
        startActivity(intent)

        // --- CORRECTION ICI ---
        // On utilise R.anim (tes fichiers) au lieu de android.R.anim (système)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        // IMPORTANT : Quand on revient sur cette page (bouton retour), on cache la vidéo
        videoContainer.visibility = View.GONE
    }
}