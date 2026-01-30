package com.example.idlemon

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Variables
        lateinit var jsonReader: JsonReader


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //lecture JSON
        jsonReader = JsonReader(this)
        val pokemonData = jsonReader.pokemons
        val movesData = jsonReader.moves

        //Création de 5 pokemon
        val tabPokemon : MutableList<Pokemon> = mutableListOf()
        val pokemon1 = jsonReader.creerPokemon(1)
        val pokemon2 = jsonReader.creerPokemon(2)
        val pokemon3 = jsonReader.creerPokemon(3)
        val pokemon4 = jsonReader.creerPokemon(4)
        val pokemon5 = jsonReader.creerPokemon(5)

        tabPokemon.add(pokemon1)
        tabPokemon.add(pokemon2)
        tabPokemon.add(pokemon3)
        tabPokemon.add(pokemon4)
        tabPokemon.add(pokemon5)

        val pokemonContainer: android.widget.LinearLayout = findViewById(R.id.pokemonContainer)
        
        for (pokemon in tabPokemon) {
            val imageView = ImageView(this)
            val params = android.widget.LinearLayout.LayoutParams(200, 200)
            params.setMargins(10, 0, 10, 0)
            imageView.layoutParams = params

            Glide.with(this)
                .load(jsonReader.getFrontSprite(pokemon.species.num))
                .into(imageView)

            pokemonContainer.addView(imageView)
        }


    }
}