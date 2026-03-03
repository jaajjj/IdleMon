package com.example.idlemon

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
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
            // Si l'équipe est vide, on vide l'affichage
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
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        //init UI
        val switchMusique = dialog.findViewById<Switch>(R.id.switchMusique)
        val switchDialogue = dialog.findViewById<Switch>(R.id.switchDialogueRapide)
        val etEmail = dialog.findViewById<EditText>(R.id.etEmail)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val btnLogin = dialog.findViewById<AppCompatButton>(R.id.btnLogin)
        val tvCreateAccount = dialog.findViewById<TextView>(R.id.tvCreateAccount)
        val tvAccountTitle = dialog.findViewById<TextView>(R.id.tvAccountTitle)
        val tvOptionsTitle = dialog.findViewById<TextView>(R.id.textView4) // Titre "Options"
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        val creditBtn = dialog.findViewById<AppCompatButton>(R.id.creditBtn)

        // --- RESET COMPTE (DISCRET) ---
        // Un appui long sur le titre "Options" reset tout (local + cloud si login)
        tvOptionsTitle.setOnLongClickListener {
            ConnexionManager.resetCompte(this, 
                onSuccess = {
                    Toast.makeText(this, "Sauvegarde réinitialisée !", Toast.LENGTH_SHORT).show()
                    updateDisplayPokemon()
                    dialog.dismiss()
                },
                onFailure = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
            )
            true
        }

        //Si connecté
        if (ConnexionManager.estConnecte()) {
            tvAccountTitle.text = "Connecté en tant que : \n${ConnexionManager.getEmail()}"
            etEmail.visibility = View.GONE
            etPassword.visibility = View.GONE
            tvCreateAccount.visibility = View.GONE
            btnLogin.text = "Déconnexion"
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED))

            btnLogin.setOnClickListener {
                ConnexionManager.deconnexion(this)
                Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
                //change la display du poké de l'accueil
                updateDisplayPokemon()
                dialog.dismiss()
            }
        } else {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    ConnexionManager.connexion(email, pass, this,
                        onSuccess = {
                            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                            //change la display du poké de l'accueil
                            updateDisplayPokemon()
                            dialog.dismiss()
                        },
                        onFailure = { error -> Toast.makeText(this, error, Toast.LENGTH_LONG).show() }
                    )
                }
            }
            tvCreateAccount.setOnClickListener {
                dialog.dismiss()
                showRegisterDialog()
            }
        }

        switchMusique.isChecked = SettingsManager.isMusicEnabled(this)
        switchDialogue.isChecked = SettingsManager.isFastDialogue(this)
        switchMusique.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setMusicEnabled(this, isChecked)
            if (isChecked) MusicManager.resume() else MusicManager.pause()
        }
        switchDialogue.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setFastDialogue(this, isChecked)
        }

        creditBtn.setOnClickListener {
            dialog.dismiss()
            showCreditsDialog()
        }
        closeBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    //boite de dialogue inscription
    private fun showRegisterDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_register)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //init UI
        val labEmail = dialog.findViewById<EditText>(R.id.etRegEmail)
        val labPass = dialog.findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = dialog.findViewById<AppCompatButton>(R.id.btnRegisterSubmit)

        dialog.findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
            dialog.dismiss()
            showSettingsDialog()
        }

        btnRegister.setOnClickListener {
            val email = labEmail.text.toString().trim()
            val pass = labPass.text.toString().trim()

            if (email.isNotEmpty() && pass.length >= 6) {
                ConnexionManager.inscription(email, pass, this,
                    onSuccess = {
                        Toast.makeText(this, "Compte créé et connecté !", Toast.LENGTH_SHORT).show()
                        //change la display du poké de l'accueil
                        updateDisplayPokemon()
                        dialog.dismiss()
                    },
                    onFailure = { error -> Toast.makeText(this, error, Toast.LENGTH_LONG).show() }
                )
            } else {
                Toast.makeText(this, "Email invalide ou MDP trop court (min 6)", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showCreditsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_credit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
            dialog.dismiss()
            showSettingsDialog()
        }
        dialog.show()
    }
}
