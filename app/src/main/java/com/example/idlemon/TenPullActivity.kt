package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class TenPullActivity : AppCompatActivity(), PanoramaUI {

    private lateinit var capteurManager: CapteurManager

    // Implémentation de PanoramaUI
    override lateinit var backgroundImage: ImageView
    override lateinit var eggsContainer: FrameLayout
    override lateinit var boussole: ImageView

    override val context: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Réutilisation du layout SinglePull pour le fond 360
        setContentView(R.layout.activity_single_pull)

        // Masquer les barres système
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Liaison des vues
        backgroundImage = findViewById(R.id.background360)
        eggsContainer = findViewById(R.id.eggsContainer)
        boussole = findViewById(R.id.boussole)
        val catchBtn = findViewById<Button>(R.id.catchBtn)

        // Initialisation : 10 oeufs, mode Ten Pull (isTenPull = true)
        capteurManager = CapteurManager(this, eggCount = 10, isTenPull = true)

        boussole.setOnClickListener {
            capteurManager.toggleMode()
        }

        catchBtn.setOnClickListener {
            val selected = capteurManager.selectedEgg
            if (selected != null) {
                // On récupère la liste (List<Pokemon>) stockée dans le tag
                val pokemonsList = selected.tag as? List<Pokemon>

                if (pokemonsList != null && pokemonsList.isNotEmpty()) {
                    // Sauvegarde des 10 Pokémons
                    pokemonsList.forEach { p -> Player.addPokemon(p) }

                    // Arrêt du capteur
                    capteurManager.stop()
                    capteurManager.cleanUpResources()

                    // Affichage de la liste
                    showResultDialog(pokemonsList)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            capteurManager.handleTouch(event)
        }
        return super.onTouchEvent(event)
    }

    private fun showResultDialog(pokemons: List<Pokemon>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.result_ten_pull)
        dialog.setCancelable(false)

        val container = dialog.findViewById<LinearLayout>(R.id.containerTenPokemons)
        val btnQuit = dialog.findViewById<Button>(R.id.quitTenPullBtn)
        val inflater = LayoutInflater.from(this)

        // Boucle pour créer chaque item de la liste
        for (pokemon in pokemons) {
            val itemView = inflater.inflate(R.layout.item_ten_pull, container, false)

            val txtNom = itemView.findViewById<TextView>(R.id.nomPok)
            val txtRarete = itemView.findViewById<TextView>(R.id.raretePok)
            val imgPoke = itemView.findViewById<ImageView>(R.id.imgPokeSmall)

            // Couleur selon rareté
            val rareteColor = when (pokemon.species.rarete) {
                "Legendaire", "Légendaire" -> Color.parseColor("#2196F3")
                "Fabuleux" -> Color.parseColor("#4CAF50")
                "Epique", "Épique" -> Color.parseColor("#9C27B0")
                "Rare" -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#000000")
            }

            txtNom.text = pokemon.species.nom
            txtNom.setTextColor(rareteColor)

            txtRarete.text = pokemon.species.rarete
            txtRarete.setTextColor(rareteColor)

            Glide.with(this)
                .asBitmap() // asBitmap est souvent plus propre pour les petites icones
                .load(DataManager.model.getFrontSprite(pokemon.species.num))
                .into(imgPoke)

            container.addView(itemView)
        }

        btnQuit.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onResume() {
        super.onResume()
        capteurManager.start()
    }

    override fun onPause() {
        super.onPause()
        capteurManager.stop()
    }
}