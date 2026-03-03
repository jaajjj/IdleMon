package com.example.idlemon

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

open class BaseActivity : AppCompatActivity() {

    //UI
    protected var pokemonDisplay: ImageView? = null

    override fun onStart() {
        super.onStart()
        MusicManager.onStartActivity()

        //init UI
        pokemonDisplay = findViewById(R.id.pokemonDisplay)
        
        //chargement local
        SaveManager.chargerLocal(this)
        //change la display du poké de l'accueil
        updateDisplayPokemon()

        //synchro cloud si login
        if (ConnexionManager.estConnecte()) {
            SaveManager.charger(this,
                onSuccess = {
                    //change la display du poké de l'accueil
                    updateDisplayPokemon()
                }
            )
        }
    }

    //change la display du poké de l'accueil
    protected fun updateDisplayPokemon() {
        if (Player.getEquipe().isNotEmpty()) {
            val leader = Player.getPremierPokemon()
            val model = ModelJson(this)

            pokemonDisplay?.let {
                Glide.with(this)
                    .load(model.getFrontSprite(leader.species.num))
                    .into(it)
            }
        } else {
            pokemonDisplay?.setImageDrawable(null)
        }
    }

    override fun onStop() {
        super.onStop()
        MusicManager.onStopActivity()
        
        //save quand on quit
        SaveManager.sauvegarderLocal(this)
        
        if (ConnexionManager.estConnecte()) {
            //save cloud si login
            SaveManager.sauvegarder()
        }
    }

    //boite de dialogue options
    fun showSettingsDialog() {
        //affiche le dialog
        SettingsManager.showSettingsDialog(this) {
            updateDisplayPokemon()
        }
    }
}
