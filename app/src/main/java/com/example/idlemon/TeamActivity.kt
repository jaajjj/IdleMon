package com.example.idlemon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class TeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        findViewById<TextView>(R.id.fieldPokegold).text = Player.getPieces().toString()

        //Log.i("TailleEquipe", Player.getEquipe().size.toString())
        afficherEquipe()

        // Navigation
        findViewById<ImageView>(R.id.gachaBtn).setOnClickListener {
            startActivity(Intent(this, GachaActivity::class.java))
        }
        findViewById<ImageView>(R.id.homeBtn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    fun afficherEquipe() {
        val equipe = Player.getEquipe()
        if (equipe.isEmpty()) return //Arrete de faire des bétises ;-;

        val leader = equipe[0]
        Log.i("test1", leader.species.nom)
        findViewById<TextView>(R.id.pokeName1).text = leader.species.nom
        Log.i("test2", leader.species.nom)
        Glide.with(this)
            .load(DataManager.model.getFrontSprite(leader.species.num))
            .into(findViewById(R.id.pokeSprite1))

        Log.i("test3", leader.species.nom)
        val type1Leader = findViewById<ImageView>(R.id.pokeType1)
        val type2Leader = findViewById<ImageView>(R.id.pokeType4)

        Log.i("test41", leader.species.type[0].toString())
        type1Leader.setImageResource(getIconType(leader.species.type[0].nom))
        Log.i("test4", leader.species.type[0].nom)
        if (leader.species.type.size > 1) {
            type2Leader.visibility = View.VISIBLE
            type2Leader.setImageResource(getIconType(leader.species.type[1].nom))
        } else {
            type2Leader.visibility = View.GONE
        }

        val dynamicContainer = findViewById<LinearLayout>(R.id.teamList)

        if (dynamicContainer.childCount > 1) {
            dynamicContainer.removeViews(1, dynamicContainer.childCount - 1)
        }

        for (i in 1 until equipe.size) {
            Log.i("Pokemon", equipe[i].species.nom)
            val pokemonView = creeViewPokemonTeam(equipe[i])
            dynamicContainer.addView(pokemonView)
        }
    }

    private fun creeViewPokemonTeam(pokemon: Pokemon): View {
        val pokemonView = layoutInflater.inflate(R.layout.item_pokemon_team, null)

        val pokeSprite = pokemonView.findViewById<ImageView>(R.id.pokeSprite)
        val pokeName = pokemonView.findViewById<TextView>(R.id.pokeName)
        val type1 = pokemonView.findViewById<ImageView>(R.id.type1)
        val type2 = pokemonView.findViewById<ImageView>(R.id.type2)

        pokeName.text = pokemon.species.nom
        Glide.with(this)
            .load(DataManager.model.getFrontSprite(pokemon.species.num))
            .into(pokeSprite)

        // Gestion des types
        type1.setImageResource(getIconType(pokemon.species.type[0].nom))
        if (pokemon.species.type.size > 1) {
            type2.visibility = View.VISIBLE
            type2.setImageResource(getIconType(pokemon.species.type[1].nom))
        } else {
            type2.visibility = View.GONE
        }
        return pokemonView
    }

    private fun getIconType(typeName: String): Int {
        return when (typeName) {
            "Acier" -> R.drawable.acier
            "Combat" -> R.drawable.combat
            "Dragon" -> R.drawable.dragon
            "Eau" -> R.drawable.eau
            "Feu" -> R.drawable.feu
            "Fee" -> R.drawable.fee
            "Glace" -> R.drawable.glace
            "Insecte" -> R.drawable.insecte
            "Normal" -> R.drawable.normal
            "Plante" -> R.drawable.plante
            "Poison" -> R.drawable.poison
            "Psy" -> R.drawable.psy
            "Roche" -> R.drawable.roche
            "Sol" -> R.drawable.sol
            "Spectre" -> R.drawable.spectre
            "Tenebre" -> R.drawable.tenebre
            "Vol" -> R.drawable.vol
            "Electrik" -> R.drawable.electrik
            else -> R.drawable.normal
        }
    }
}