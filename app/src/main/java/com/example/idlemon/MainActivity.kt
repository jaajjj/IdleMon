package com.example.idlemon

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    //UI
    private lateinit var pokemonDisplay: ImageView
    private lateinit var fieldPokegold: TextView
    private lateinit var playBtn: ImageView
    private lateinit var homeBtn: ImageView
    private lateinit var teamBtn: ImageView
    private lateinit var gachaBtn: ImageView
    private lateinit var settingsBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        DataManager.setup(this)
        val modelJson = DataManager.model

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //cache les barres sys
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        pokemonDisplay = findViewById(R.id.pokemonDisplay)
        playBtn = findViewById(R.id.playBtn)
        homeBtn = findViewById(R.id.homeBtn)
        teamBtn = findViewById(R.id.teamBtn)
        gachaBtn = findViewById(R.id.gachaBtn)
        fieldPokegold = findViewById(R.id.fieldPokegold)
        settingsBtn = findViewById(R.id.settingsBtn)

        val player = Player

        if (Player.getEquipe().isEmpty()) {
            Player.addEquipe(modelJson.creerPokemon("Victini"))
            Player.addEquipe(modelJson.creerPokemon(94))
            Player.addEquipe(modelJson.creerPokemon(122))
            Player.addEquipe(modelJson.creerPokemon(774))
        }

        if (Player.getNbPokemon() == 0) {
            player.addPokemon(modelJson.creerPokemon(653))
            player.addPokemon(modelJson.creerPokemon(264))
            player.addPokemon(modelJson.creerPokemon(298))
            player.addPokemon(modelJson.creerPokemon(419))
            player.addPokemon(modelJson.creerPokemon(159))
            player.addPokemon(modelJson.creerPokemon(817))
            player.addPokemon(modelJson.creerPokemon(180))
            player.addPokemon(modelJson.creerPokemon(191))
        }

        //setups et listener
        fieldPokegold.text = player.getPieces().toString()
        Glide.with(this)
            .load(modelJson.getFrontSprite(player.getPremierPokemon().species.num))
            .into(pokemonDisplay)

        teamBtn.setOnClickListener {
            val intent = Intent(this, TeamActivity::class.java)
            startActivity(intent)
        }

        gachaBtn.setOnClickListener {
            val intent = Intent(this, GachaActivity::class.java)
            startActivity(intent)
        }
        playBtn.setOnClickListener {
            val intent = Intent(this, PlayActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Player.getEquipe().isNotEmpty()) {
            Glide.with(this)
                .load(DataManager.model.getFrontSprite(Player.getPremierPokemon().species.num))
                .into(pokemonDisplay)
        }
    }
}