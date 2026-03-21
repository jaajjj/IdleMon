package com.example.idlemon

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable

@SuppressLint("StaticFieldLeak") // C'était juste relou d'avoir tout souligné en jaune ;-;
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

    // --- OUTILS DE POPUPS NATIFS ---
    private fun showSuccessPopup(activity: Activity, title: String, message: String, onDismiss: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Super !") { d, _ ->
                d.dismiss()
                onDismiss()
            }
            .setCancelable(false) // Empêche de fermer en cliquant à côté, force à lire !
            .show()
    }

    private fun showConfirmPopup(activity: Activity, title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Oui") { d, _ ->
                d.dismiss()
                onConfirm()
            }
            .setNegativeButton("Annuler") { d, _ ->
                d.dismiss()
            }
            .show()
    }

    //boite de dialogue options
    fun showSettingsDialog(activity: Activity, onRefreshUI: () -> Unit) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        //init les vues
        initViewsSettings(dialog)

        //reset compte sur titre Options
        tvOptionsTitle.setOnLongClickListener {
            showConfirmPopup(activity, "Réinitialiser", "Voulez-vous vraiment effacer votre sauvegarde ? Cette action est irréversible.") {
                ConnexionManager.resetCompte(activity,
                    onSuccess = {
                        showSuccessPopup(activity, "Réinitialisé", "La sauvegarde a été réinitialisée.") {
                            onRefreshUI()
                            dialog.dismiss()
                        }
                    },
                    onFailure = { Toast.makeText(activity, it, Toast.LENGTH_SHORT).show() }
                )
            }
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
                showConfirmPopup(activity, "Déconnexion", "Voulez-vous vraiment vous déconnecter ?") {
                    ConnexionManager.deconnexion(activity)
                    SaveManager.sauvegarderLocal(activity)
                    showSuccessPopup(activity, "Au revoir", "Vous avez bien été déconnecté.") {
                        onRefreshUI()
                        dialog.dismiss()
                    }
                }
            }
        } else {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    ConnexionManager.connexion(email, pass, activity,
                        onSuccess = {
                            showSuccessPopup(activity, "Bon retour !", "Vous êtes maintenant connecté.") {
                                onRefreshUI()
                                dialog.dismiss()
                            }
                        },
                        onFailure = { error -> Toast.makeText(activity, error, Toast.LENGTH_LONG).show() }
                    )
                }
            }

            //Connexion Google
            btnGoogle.setOnClickListener {
                ConnexionManager.connexionGoogle(activity,
                    onSuccess = {
                        showSuccessPopup(activity, "Connexion Google", "Connexion réussie ! Vos données sont synchronisées.") {
                            onRefreshUI()
                            dialog.dismiss()
                        }
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
        val metrics = activity.resources.displayMetrics
        val width = (metrics.widthPixels * 0.90).toInt()
        val height = (metrics.heightPixels * 0.90).toInt()

        dialog.window?.setLayout(width, height)

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
                        showSuccessPopup(activity, "Bienvenue !", "Votre compte a été créé avec succès.") {
                            onRefreshUI()
                            dialog.dismiss()
                        }
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

        val metrics = activity.resources.displayMetrics
        val width = (metrics.widthPixels * 0.90).toInt()
        val height = (metrics.heightPixels * 0.90).toInt()

        dialog.window?.setLayout(width, height)

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