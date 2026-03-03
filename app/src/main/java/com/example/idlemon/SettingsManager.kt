package com.example.idlemon

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable

@SuppressLint("StaticFieldLeak") //C'était juste relou d'avoir tout souligné en jaune ;-;
object SettingsManager {
    private const val NOM_FI_PARAM = "MesParametres"
    private const val KEY_MUSIC = "music_enabled"
    private const val KEY_FAST_DIALOGUE = "fast_dialogue"

    //UI Settings
    private lateinit var switchMusique: Switch
    private lateinit var switchDialogue: Switch
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var btnGoogle: View
    private lateinit var tvCreateAccount: TextView
    private lateinit var tvAccountTitle: TextView
    private lateinit var tvOptionsTitle: TextView
    private lateinit var closeBtn: ImageView
    private lateinit var creditBtn: AppCompatButton

    //UI Register
    private lateinit var labEmail: EditText
    private lateinit var labPass: EditText
    private lateinit var btnRegister: AppCompatButton
    private lateinit var closeBtnReg: ImageView

    //UI Credits
    private lateinit var closeBtnCredits: ImageView

    fun isMusicEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(NOM_FI_PARAM, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MUSIC, true)
    }

    fun setMusicEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(NOM_FI_PARAM, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_MUSIC, enabled) }
    }

    fun isFastDialogue(context: Context): Boolean {
        val prefs = context.getSharedPreferences(NOM_FI_PARAM, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FAST_DIALOGUE, false)
    }

    fun setFastDialogue(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(NOM_FI_PARAM, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_FAST_DIALOGUE, enabled) }
    }

    //boite de dialogue options
    fun showSettingsDialog(activity: Activity, onRefreshUI: () -> Unit) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        //init les vues
        initViewsSettings(dialog)

        // Reset compte sur titre Options
        tvOptionsTitle.setOnLongClickListener {
            ConnexionManager.resetCompte(activity, 
                onSuccess = {
                    Toast.makeText(activity, "Sauvegarde réinitialisée !", Toast.LENGTH_SHORT).show()
                    onRefreshUI()
                    dialog.dismiss()
                },
                onFailure = { Toast.makeText(activity, it, Toast.LENGTH_SHORT).show() }
            )
            true
        }

        //Si connecté
        if (ConnexionManager.estConnecte()) {
            tvAccountTitle.text = "Connecté en tant que : \n${ConnexionManager.getEmail()}"
            etEmail.visibility = View.GONE
            etPassword.visibility = View.GONE
            btnGoogle.visibility = View.GONE
            tvCreateAccount.visibility = View.GONE
            btnLogin.text = "Déconnexion"
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED))

            btnLogin.setOnClickListener {
                ConnexionManager.deconnexion(activity)
                SaveManager.sauvegarderLocal(activity)
                Toast.makeText(activity, "Déconnecté", Toast.LENGTH_SHORT).show()
                onRefreshUI()
                dialog.dismiss()
            }
        } else {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    ConnexionManager.connexion(email, pass, activity,
                        onSuccess = {
                            Toast.makeText(activity, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                            onRefreshUI()
                            dialog.dismiss()
                        },
                        onFailure = { error -> Toast.makeText(activity, error, Toast.LENGTH_LONG).show() }
                    )
                }
            }
            
            //Connexion Google
            btnGoogle.setOnClickListener {
                ConnexionManager.connexionGoogle(activity,
                    onSuccess = {
                        Toast.makeText(activity, "Connecté avec Google !", Toast.LENGTH_SHORT).show()
                        onRefreshUI()
                        dialog.dismiss()
                    },
                    onFailure = { error -> Toast.makeText(activity, error, Toast.LENGTH_LONG).show() }
                )
            }

            tvCreateAccount.setOnClickListener {
                dialog.dismiss()
                showRegisterDialog(activity, onRefreshUI)
            }
        }

        switchMusique.isChecked = isMusicEnabled(activity)
        switchDialogue.isChecked = isFastDialogue(activity)
        
        switchMusique.setOnCheckedChangeListener { _, isChecked ->
            setMusicEnabled(activity, isChecked)
            if (isChecked) MusicManager.resume() else MusicManager.pause()
        }
        switchDialogue.setOnCheckedChangeListener { _, isChecked ->
            setFastDialogue(activity, isChecked)
        }

        creditBtn.setOnClickListener {
            dialog.dismiss()
            showCreditsDialog(activity)
        }
        closeBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun initViewsSettings(dialog: Dialog) {
        switchMusique = dialog.findViewById(R.id.switchMusique)
        switchDialogue = dialog.findViewById(R.id.switchDialogueRapide)
        etEmail = dialog.findViewById(R.id.etEmail)
        etPassword = dialog.findViewById(R.id.etPassword)
        btnLogin = dialog.findViewById(R.id.btnLogin)
        btnGoogle = dialog.findViewById(R.id.btnGoogleSignInLayout)
        tvCreateAccount = dialog.findViewById<TextView>(R.id.tvCreateAccount)
        tvAccountTitle = dialog.findViewById(R.id.tvAccountTitle)
        tvOptionsTitle = dialog.findViewById(R.id.textView4)
        closeBtn = dialog.findViewById(R.id.closeBtn)
        creditBtn = dialog.findViewById(R.id.creditBtn)
    }

    //boite de dialogue inscription
    private fun showRegisterDialog(activity: Activity, onRefreshUI: () -> Unit) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_register)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //init les vues
        initViewsRegister(dialog)

        closeBtnReg.setOnClickListener {
            dialog.dismiss()
            showSettingsDialog(activity, onRefreshUI)
        }

        btnRegister.setOnClickListener {
            val email = labEmail.text.toString().trim()
            val pass = labPass.text.toString().trim()

            if (email.isNotEmpty() && pass.length >= 6) {
                ConnexionManager.inscription(email, pass, activity,
                    onSuccess = {
                        Toast.makeText(activity, "Compte créé !", Toast.LENGTH_SHORT).show()
                        onRefreshUI()
                        dialog.dismiss()
                    },
                    onFailure = { error -> Toast.makeText(activity, error, Toast.LENGTH_LONG).show() }
                )
            } else {
                Toast.makeText(activity, "Email invalide ou MDP trop court (min 6)", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun initViewsRegister(dialog: Dialog) {
        labEmail = dialog.findViewById(R.id.etRegEmail)
        labPass = dialog.findViewById(R.id.etRegPassword)
        btnRegister = dialog.findViewById(R.id.btnRegisterSubmit)
        closeBtnReg = dialog.findViewById(R.id.closeBtn)
    }

    //boite de dialogue credits
    private fun showCreditsDialog(activity: Activity) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_credit)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        
        //init les vues
        initViewsCredits(dialog)

        closeBtnCredits.setOnClickListener {
            dialog.dismiss()
            showSettingsDialog(activity, {})
        }
        dialog.show()
    }

    private fun initViewsCredits(dialog: Dialog) {
        closeBtnCredits = dialog.findViewById(R.id.closeBtn)
    }
}
