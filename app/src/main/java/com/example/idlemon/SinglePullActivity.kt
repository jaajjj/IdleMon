package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class SinglePullActivity : AppCompatActivity() {

    //UI
    private lateinit var capteurManager: CapteurManager
    lateinit var backgroundImage: ImageView
    lateinit var eggsContainer: FrameLayout
    lateinit var boussole: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_pull)

        //sans barre sys
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //background et egg init
        backgroundImage = findViewById(R.id.background360)
        eggsContainer = findViewById(R.id.eggsContainer)
        boussole = findViewById(R.id.boussole) // Init boussole
        val catchBtn = findViewById<Button>(R.id.catchBtn)

        //capteur
        capteurManager = CapteurManager(this)

        // Gestion du click boussole (Sensor vs Doigt)
        boussole.setOnClickListener {
            capteurManager.toggleMode()
        }

        catchBtn.setOnClickListener {
            val selected = capteurManager.selectedEgg
            if (selected != null) {
                val pokemon = selected.tag as Pokemon
                Player.addPokemon(pokemon)

                capteurManager.stop() //fige le fond (stop sensor)
                capteurManager.cleanUpResources() //nettoie la mémoire (stop lag)

                showResultDialog(pokemon)
            }
        }
    }

    // Intercepte le tactile pour le mode manuel
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            capteurManager.handleTouch(event)
        }
        return super.onTouchEvent(event)
    }

    private fun showResultDialog(pokemon: Pokemon) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.result_single_pull)
        dialog.setCancelable(false) // Empeche l'interaction avec le fond

        val txtNom = dialog.findViewById<TextView>(R.id.pokemonNomPull)
        val txtRarete = dialog.findViewById<TextView>(R.id.raretePull)
        val imgPoke = dialog.findViewById<ImageView>(R.id.imgPokemonPull)
        val btnQuit = dialog.findViewById<Button>(R.id.quitPullBtn)

        //couleur par rareté
        val rareteColor = when (pokemon.species.rarete) {
            "Légendaire", "Legendaire" -> Color.parseColor("#2196F3")
            "Fabuleux" -> Color.parseColor("#4CAF50")
            "Épique", "Epique" -> Color.parseColor("#9C27B0")
            "Rare" -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#000000")
        }

        txtNom.text = pokemon.species.nom
        txtNom.setTextColor(rareteColor)

        txtRarete.text = pokemon.species.rarete
        txtRarete.setTextColor(rareteColor)

        Glide.with(this)
            .asGif()
            .load(DataManager.model.getFrontSprite(pokemon.species.num))
            .into(imgPoke)

        // Action du bouton Récupérer
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