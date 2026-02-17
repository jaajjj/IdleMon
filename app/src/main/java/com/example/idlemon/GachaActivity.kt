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

class GachaActivity : BaseActivity() {

    //UI
    private lateinit var homeBtn: ImageView
    private lateinit var teamBtn: ImageView
    private lateinit var singlePullBtn: Button
    private lateinit var tenPullBtn: Button
    private lateinit var fieldPokegold: TextView

    //UI Vidéo
    private lateinit var videoContainer: FrameLayout
    private lateinit var summonVideoView: VideoView
    private lateinit var skipBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gacha)

        //pleine écran
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //init vues
        homeBtn = findViewById(R.id.homeBtn)
        teamBtn = findViewById(R.id.teamBtn)
        singlePullBtn = findViewById(R.id.singlePullBtn)
        tenPullBtn = findViewById(R.id.tenPullBtn)
        fieldPokegold = findViewById(R.id.fieldPokegold)

        //init vue vidéo
        videoContainer = findViewById(R.id.videoContainer)
        summonVideoView = findViewById(R.id.summonVideoView)
        skipBtn = findViewById(R.id.skipBtn)

        val player = Player
        fieldPokegold.text = player.getPieces().toString()

        //nav footer
        teamBtn.setOnClickListener { startActivity(Intent(this, TeamActivity::class.java)) }
        homeBtn.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        singlePullBtn.setOnClickListener {
            if (player.getPieces() >= 100) {
                player.setPieces(player.getPieces() - 100)
                fieldPokegold.text = player.getPieces().toString()

                //vid 1 pull
                playSummonAnimation(1)
            }
        }

        tenPullBtn.setOnClickListener {
            if (player.getPieces() >= 1000) {
                player.setPieces(player.getPieces() - 1000)
                fieldPokegold.text = player.getPieces().toString()

                //vid 10 pull
                playSummonAnimation(10)
            }
        }
    }

    private fun playSummonAnimation(nbVoeux: Int) {
        videoContainer.visibility = View.VISIBLE
        val videoPath = "android.resource://" + packageName + "/" + R.raw.anim_gacha
        val uri = Uri.parse(videoPath)
        summonVideoView.setVideoURI(uri)
        summonVideoView.setOnCompletionListener {
            goToResultActivity(nbVoeux)
        }
        skipBtn.setOnClickListener {
            summonVideoView.stopPlayback()
            goToResultActivity(nbVoeux)
        }

        //lance la vid
        summonVideoView.start()
    }

    private fun goToResultActivity(nb: Int) {
        //lance l'activitée suivante
        val intent = if (nb == 1) {
            Intent(this, SinglePullActivity::class.java)
        } else {
            Intent(this, TenPullActivity::class.java)
        }
        startActivity(intent)
        //ajout du fadeIn et out
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        //on cache la vidéo quand on reviens sur la page
        videoContainer.visibility = View.GONE
    }
}