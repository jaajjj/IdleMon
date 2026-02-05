package com.example.idlemon

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GachaActivity : AppCompatActivity() {

    //UI
    private lateinit var homeBtn: ImageView
    private lateinit var teamBtn: ImageView
    private lateinit var settingsBtn: ImageView
    private lateinit var singlePullBtn: Button
    private lateinit var tenPullBtn: Button
    private lateinit var fieldPokegold: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gacha)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //barres sys
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        homeBtn = findViewById(R.id.homeBtn)
        teamBtn = findViewById(R.id.teamBtn)
        settingsBtn = findViewById(R.id.settingsBtn)
        singlePullBtn = findViewById(R.id.singlePullBtn)
        tenPullBtn = findViewById(R.id.tenPullBtn)
        fieldPokegold = findViewById(R.id.fieldPokegold)

        //data + player
        val player = Player
        fieldPokegold.text = player.getPieces().toString()

        //Nav et pull
        teamBtn.setOnClickListener {
            val intent = Intent(this, TeamActivity::class.java)
            startActivity(intent)
        }

        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        singlePullBtn.setOnClickListener {
            if (player.getPieces() >= 100) {
                player.setPieces(player.getPieces() - 100)
                fieldPokegold.text = player.getPieces().toString()
                switchAnimGacha(1)
            }
        }

        tenPullBtn.setOnClickListener {
            if (player.getPieces() >= 1000) {
                player.setPieces(player.getPieces() - 1000)
                fieldPokegold.text = player.getPieces().toString()
                switchAnimGacha(10)
            }
        }
    }

    //nav gacha pull
    fun switchAnimGacha(nb: Int) {
        if (nb == 1) {
            val intent = Intent(this, SinglePullActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, TenPullActivity::class.java)
            startActivity(intent)
        }
    }
}