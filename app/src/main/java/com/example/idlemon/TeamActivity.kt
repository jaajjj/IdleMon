package com.example.idlemon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class TeamActivity : BaseActivity() {

    //UI
    private lateinit var pokegold: TextView
    private lateinit var changeTeamBtn: ImageView
    private lateinit var settingsBtn: ImageView
    private lateinit var homeBtn: ImageView
    private lateinit var gachaBtn: ImageView
    private lateinit var teamList: LinearLayout
    private lateinit var pokedexBtn: ImageView


    //Leader
    private lateinit var pokeSprite1: ImageView
    private lateinit var pokeName1: TextView
    private lateinit var type1Leader: ImageView
    private lateinit var type2Leader: ImageView
    private lateinit var attackBtn1: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team)

        //init les vues
        initViews()

        // 🚨 Correction du crash : appel de l'ID teamPage au lieu de homePage
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.teamPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Sans barre sys
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //Data et listener
        refreshUI()

        gachaBtn.setOnClickListener {
            startActivity(Intent(this, GachaActivity::class.java))
        }

        homeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        settingsBtn.setOnClickListener {
            showSettingsDialog()
        }

        changeTeamBtn.setOnClickListener {
            val dialog = ChangeTeamDialog(this)
            dialog.dialog.setOnDismissListener { afficherEquipe() }
            dialog.show()
        }
        pokedexBtn.setOnClickListener {
            startActivity(Intent(this, PokedexActivity::class.java))
        }
    }

    private fun initViews() {
        pokegold = findViewById(R.id.fieldPokegold)
        changeTeamBtn = findViewById(R.id.changeTeamBtn)
        homeBtn = findViewById(R.id.homeBtn)
        gachaBtn = findViewById(R.id.gachaBtn)
        teamList = findViewById(R.id.teamList)
        settingsBtn = findViewById(R.id.settingsBtn)
        pokedexBtn = findViewById<ImageView>(R.id.pokedexBtn)

        //Leader UI
        pokeSprite1 = findViewById(R.id.pokeSprite1)
        pokeName1 = findViewById(R.id.pokeName1)
        type1Leader = findViewById(R.id.pokeType1)
        type2Leader = findViewById(R.id.pokeType4)
        attackBtn1 = findViewById(R.id.attackBtn1)

    }

    override fun refreshUI() {
        super.refreshUI()
        if (::pokegold.isInitialized) {
            pokegold.text = Player.getPieces().toString()
        }
        afficherEquipe()
    }

    fun afficherEquipe() {
        val equipe = Player.getEquipe()
        if (equipe.isEmpty()) {
            pokeName1.text = ""
            pokeSprite1.setImageDrawable(null)
            type1Leader.visibility = View.GONE
            type2Leader.visibility = View.GONE
            if (teamList.childCount > 1) teamList.removeViews(1, teamList.childCount - 1)
            return
        }

        //Leader
        val leader = equipe[0]
        pokeName1.text = leader.species.nom

        Glide.with(this)
            .load(DataManager.model.getFrontSprite(leader.species.num))
            .fitCenter()
            .into(pokeSprite1)

        pokeSprite1.setOnClickListener {
            val dialog = ChangeTeamDialog(this)
            dialog.dialog.setOnDismissListener { afficherEquipe() }
            dialog.show()
        }

        attackBtn1.setOnClickListener {
            ChangeAttackDialog(this, leader).show()
        }

        //Type 1
        type1Leader.visibility = View.VISIBLE
        type1Leader.setImageResource(DataManager.model.getIconType(leader.species.type[0].nom))

        //Type 2
        if (leader.species.type.size > 1) {
            type2Leader.visibility = View.VISIBLE
            type2Leader.setImageResource(DataManager.model.getIconType(leader.species.type[1].nom))
        } else {
            type2Leader.visibility = View.GONE
        }

        //Team list
        if (teamList.childCount > 1) {
            teamList.removeViews(1, teamList.childCount - 1)
        }
        for (i in 1 until equipe.size) {
            val pokemonView = creeViewPokemonTeam(equipe[i])
            teamList.addView(pokemonView)
        }
    }

    private fun creeViewPokemonTeam(pokemon: Pokemon): View {
        // 🚨 Correction d'affichage : on attache virtuellement la vue à teamList
        val pokemonView = layoutInflater.inflate(R.layout.item_pokemon_team, teamList, false)

        val pokeSprite = pokemonView.findViewById<ImageView>(R.id.obj1)
        val pokeName = pokemonView.findViewById<TextView>(R.id.pokeName)
        val type1 = pokemonView.findViewById<ImageView>(R.id.type1)
        val type2 = pokemonView.findViewById<ImageView>(R.id.type2)
        val attackBtn = pokemonView.findViewById<ImageView>(R.id.attackBtn1)

        pokeName.text = pokemon.species.nom

        Glide.with(this)
            .load(DataManager.model.getFrontSprite(pokemon.species.num))
            .fitCenter()
            .into(pokeSprite)

        pokeSprite.setOnClickListener {
            val dialog = ChangeTeamDialog(this)
            dialog.dialog.setOnDismissListener { afficherEquipe() }
            dialog.show()
        }

        attackBtn.setOnClickListener {
            ChangeAttackDialog(this, pokemon).show()
        }

        type1.setImageResource(DataManager.model.getIconType(pokemon.species.type[0].nom))

        if (pokemon.species.type.size > 1) {
            type2.visibility = View.VISIBLE
            type2.setImageResource(DataManager.model.getIconType(pokemon.species.type[1].nom))
        } else {
            type2.visibility = View.GONE
        }
        return pokemonView
    }
}