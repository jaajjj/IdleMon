package com.example.idlemon

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class GachaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gacha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Cache la barre overlay du téléphone (haut et bas)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //Json
        var modelJson = DataManager.model

        //var xml
        val pokemonDisplay = findViewById<ImageView>(R.id.pokemonDisplay)
        val playBtn = findViewById<ImageView>(R.id.playBtn)
        val homeBtn = findViewById<ImageView>(R.id.homeBtn)
        val teamBtn = findViewById<ImageView>(R.id.teamBtn)
        val gachaBtn = findViewById<ImageView>(R.id.gachaBtn)
        val fieldPokegold = findViewById<TextView>(R.id.fieldPokegold)
        val settingsBtn = findViewById<ImageView>(R.id.settingsBtn)


        //Récup Player
        val player = Player
        //mettre le frontSprite du premier pokemon de la liste du Player
        Glide.with(this).load(modelJson.getFrontSprite(player.getPremierPokemon().species.num )).into(pokemonDisplay)


        // Redirection vers l'équipe
        teamBtn.setOnClickListener {
            val intent = Intent(this, TeamActivity::class.java)
            startActivity(intent)
        }

        // Redirection vers le Home
        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


}