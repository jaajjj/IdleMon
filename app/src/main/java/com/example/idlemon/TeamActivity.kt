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
        //enlever les barres du systeme
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //var xml
        val pokegold = findViewById<TextView>(R.id.fieldPokegold)
        val changeTeamBtn = findViewById<ImageView>(R.id.changeTeamBtn)
        val homeBtn = findViewById<ImageView>(R.id.homeBtn)
        val gachadBtn = findViewById<ImageView>(R.id.gachaBtn)

        pokegold.text = Player.getPieces().toString()

        afficherEquipe()

        //footer
        gachadBtn.setOnClickListener {
            startActivity(Intent(this, GachaActivity::class.java))
        }
        homeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        changeTeamBtn.setOnClickListener {
            val dialog = ChangeTeamDialog(this)
            dialog.dialog.setOnDismissListener {
                afficherEquipe() //pour actualiser l'équipe quand le dialog se ferme
            }
            dialog.show()
        }
    }

    fun afficherEquipe() {
        val equipe = Player.getEquipe()
        if (equipe.isEmpty()) return //Arrete de faire des bétises ;-;

        //leader
        val leader = equipe[0]
        val pokeSprite1 = findViewById<ImageView>(R.id.pokeSprite1)
        val pokeName1 = findViewById<TextView>(R.id.pokeName1)

        pokeName1.text = leader.species.nom
        Glide.with(this)
            .load(DataManager.model.getFrontSprite(leader.species.num))
            .fitCenter()
            .into(pokeSprite1)

        val type1Leader = findViewById<ImageView>(R.id.pokeType1)
        val type2Leader = findViewById<ImageView>(R.id.pokeType4)
        val attackBtn1 = findViewById<ImageView>(R.id.attackBtn1)
        pokeSprite1.setOnClickListener {
            val dialog = ChangeTeamDialog(this)
            dialog.dialog.setOnDismissListener {
                afficherEquipe()
            }
            dialog.show()
        }
        attackBtn1.setOnClickListener {
            ChangeAttackDialog(this, leader).show()
        }

        type1Leader.setImageResource(getIconType(leader.species.type[0].nom))

        if (leader.species.type.size > 1) {
            type2Leader.visibility = View.VISIBLE
            type2Leader.setImageResource(getIconType(leader.species.type[1].nom))
        } else {
            type2Leader.visibility = View.GONE
        }

        //teamList
        val teamList = findViewById<LinearLayout>(R.id.teamList)
        //on vide pour pas doubler
        if (teamList.childCount > 1) {
            teamList.removeViews(1, teamList.childCount - 1)
        }
        for (i in 1 until equipe.size) {
            Log.i("Pokemon", equipe[i].species.nom)
            val pokemonView = creeViewPokemonTeam(equipe[i])
            teamList.addView(pokemonView)
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
            .fitCenter()
            .into(pokeSprite)

        pokeSprite.setOnClickListener {
            val dialog = ChangeTeamDialog(this)
            dialog.dialog.setOnDismissListener {
                afficherEquipe()
            }
            dialog.show()
        }

        val attackBtn = pokemonView.findViewById<ImageView>(R.id.attackBtn1)
        attackBtn.setOnClickListener {
            ChangeAttackDialog(this, pokemon).show()
        }

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