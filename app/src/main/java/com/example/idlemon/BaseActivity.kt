package com.example.idlemon

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

open class BaseActivity : AppCompatActivity() {

    //UI
    protected var pokemonDisplayUmageView: ImageView? = null

    override fun onStart() {
        super.onStart()
        MusicManager.onStartActivity()

        //init UI
        pokemonDisplayUmageView = findViewById(R.id.pokemonDisplay)
        
        //chargement local
        SaveManager.chargerLocal(this)
        
        //refresh global de l'UI
        refreshUI()
    }

    //refresh global de l'UI
    open fun refreshUI() {
        //change la display du poké de l'accueil
        updateDisplayPokemon()
    }

    //change la display du poké de l'accueil
    protected fun updateDisplayPokemon() {
        if (Player.getEquipe().isNotEmpty()) {
            val leader = Player.getPremierPokemon()
            val model = ModelJson(this)

            pokemonDisplayUmageView?.let {
                Glide.with(this)
                    .load(model.getFrontSprite(leader.species.num))
                    .into(it)
            }
        } else {
            pokemonDisplayUmageView?.setImageDrawable(null)
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
        SettingsManager.showSettingsDialog(this) {
            //refresh l'accueil après login/logout/reset
            refreshUI()
        }
    }
}
