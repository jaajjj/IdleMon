package com.example.idlemon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class MainActivity : BaseActivity() {

    //UI
    private lateinit var pokemonDisplay: ImageView
    private lateinit var fieldPokegold: TextView
    private lateinit var playBtn: ImageView
    private lateinit var homeBtn: ImageView
    private lateinit var teamBtn: ImageView
    private lateinit var gachaBtn: ImageView
    private lateinit var settingsBtn: ImageView

    private lateinit var videoContainer: FrameLayout
    private lateinit var summonGifView: ImageView
    private var isOnAnim = false


    override fun onCreate(savedInstanceState: Bundle?) {
        DataManager.setup(this)
        MusicManager.setup(this)

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

        videoContainer = findViewById(R.id.videoContainer)
        summonGifView = findViewById(R.id.summonGifView)

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
            if (!isOnAnim) {
                val intent = Intent(this, TeamActivity::class.java)
                startActivity(intent)
            }
        }

        gachaBtn.setOnClickListener {
            if (!isOnAnim) {
                val intent = Intent(this, GachaActivity::class.java)
                startActivity(intent)
            }
        }

        playBtn.setOnClickListener {
            if (!isOnAnim) {
                animPlayBtn()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // On réactive les boutons quand on revient sur l'activité
        isOnAnim = false
        videoContainer.visibility = View.GONE

        if (Player.getEquipe().isNotEmpty()) {
            Glide.with(this)
                .load(DataManager.model.getFrontSprite(Player.getPremierPokemon().species.num))
                .into(pokemonDisplay)
        }
    }

    private fun animPlayBtn() {
        //on verrouille les clics
        isOnAnim = true

        MusicManager.lancerSequenceCombat(this)

        videoContainer.visibility = View.VISIBLE

        Glide.with(this)
            .load(R.drawable.transition_battle)
            .into(summonGifView)

        val gifDurationInMillis: Long = 2900

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, PlayActivity::class.java)
            startActivity(intent)
        }, gifDurationInMillis)
    }
}