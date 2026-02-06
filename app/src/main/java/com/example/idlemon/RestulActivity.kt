package com.example.idlemon

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_single_pull)

        // On récupère les infos passées par l'Intent
        val pokemonName = intent.getStringExtra("POKEMON_NAME")
        val pokemonRarete = intent.getStringExtra("POKEMON_RARETE")
        val pokemonNum = intent.getIntExtra("POKEMON_NUM", 0)

        val txtNom = findViewById<TextView>(R.id.pokemonNomPull)
        val txtRarete = findViewById<TextView>(R.id.raretePull)
        val imgPoke = findViewById<ImageView>(R.id.imgPokemonPull)
        val btnQuit = findViewById<Button>(R.id.quitPullBtn)

        // Logique de couleur (identique à ton code)
        val rareteColor = when (pokemonRarete) {
            "Légendaire", "Legendaire" -> Color.parseColor("#2196F3")
            "Fabuleux" -> Color.parseColor("#4CAF50")
            "Épique", "Epique" -> Color.parseColor("#9C27B0")
            "Rare" -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#000000")
        }

        txtNom.text = pokemonName
        txtNom.setTextColor(rareteColor)
        txtRarete.text = pokemonRarete
        txtRarete.setTextColor(rareteColor)

        Glide.with(this)
            .asGif()
            .load(DataManager.model.getFrontSprite(pokemonNum))
            .into(imgPoke)

        btnQuit.setOnClickListener {
            finish()
        }
    }
}