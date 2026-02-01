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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        //lecture JSON
        DataManager.setup(this)
        var modelJson = DataManager.model

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Cache la barre overlay du téléphone (haut et bas)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //Init var xml
        val pokemonDisplay = findViewById<ImageView>(R.id.pokemonDisplay)
        val playBtn = findViewById<ImageView>(R.id.playBtn)
        val homeBtn = findViewById<ImageView>(R.id.homeBtn)
        val teamBtn = findViewById<ImageView>(R.id.teamBtn)
        val gachaBtn = findViewById<ImageView>(R.id.gachaBtn)
        val fieldPokegold = findViewById<TextView>(R.id.fieldPokegold)
        val settingsBtn = findViewById<ImageView>(R.id.settingsBtn)


        //Init player
        val player = Player
        if (Player.getEquipe().isEmpty()) {
            Player.addEquipe(modelJson.creerPokemon("Victini"))
            Player.addEquipe(modelJson.creerPokemon(98))
            Player.addEquipe(modelJson.creerPokemon(322))
            Player.addEquipe(modelJson.creerPokemon(544))
        }

        if (Player.getNbPokemon() == 0) {
            player.addPokemon(modelJson.creerPokemon(653))
            player.addPokemon(modelJson.creerPokemon(94))
            player.addPokemon(modelJson.creerPokemon(2))
            player.addPokemon(modelJson.creerPokemon(4))
            player.addPokemon(modelJson.creerPokemon(15))
            player.addPokemon(modelJson.creerPokemon(87))
            player.addPokemon(modelJson.creerPokemon(100))
            player.addPokemon(modelJson.creerPokemon(101))
        }


        //mettre le frontSprite du premier pokemon de la liste du Player
        Glide.with(this).load(modelJson.getFrontSprite(player.getPremierPokemon().species.num )).into(pokemonDisplay)

        //redirection vers l'équipe
        teamBtn.setOnClickListener {
            val intent = Intent(this, TeamActivity::class.java)
            startActivity(intent)
        }

        //redirection vers le Gacha
        gachaBtn.setOnClickListener {
            val intent = Intent(this, GachaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val pokemonDisplay = findViewById<ImageView>(R.id.pokemonDisplay)
        if (Player.getEquipe().isNotEmpty()) {
            Glide.with(this)
                .load(DataManager.model.getFrontSprite(Player.getPremierPokemon().species.num))
                .into(pokemonDisplay)
        }
    }
}