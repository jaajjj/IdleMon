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
import androidx.core.text.HtmlCompat

@SuppressLint("StaticFieldLeak")
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
    private lateinit var rulesBtn: AppCompatButton // <-- Nouveau bouton

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
            .setCancelable(false)
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

        // --- CLIC SUR LE BOUTON RÈGLES ---
        rulesBtn.setOnClickListener {
            dialog.dismiss()
            showRulesDialog(activity)
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
        rulesBtn = dialog.findViewById(R.id.rulesBtn) // <-- ID CORRIGÉ ICI
    }

    private fun showRulesDialog(activity: Activity) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_rules)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val width = (activity.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val closeRulesBtn = dialog.findViewById<ImageView>(R.id.closeRulesBtn)
        val imgRule = dialog.findViewById<ImageView>(R.id.imgRule)
        val tvRuleText = dialog.findViewById<TextView>(R.id.tvRuleText)
        val btnPrevRule = dialog.findViewById<ImageView>(R.id.btnPrevRule)
        val btnNextRule = dialog.findViewById<ImageView>(R.id.btnNextRule)
        val tvPageIndicator = dialog.findViewById<TextView>(R.id.tvPageIndicator)

        // Structure de données pour stocker chaque page (Image + Texte avec mots clés)
        data class RulePage(val imageRes: Int, val htmlText: String)

        // Textes et images à volonté
        val pages = listOf(
            RulePage(R.drawable.attack_btn_battle, "Affrontez des <font color='#D84315'><b>Vagues d'ennemis</b></font> sans fin pour gagner de l'expérience et du butin. Tous les 10 niveaux, un <font color='#D84315'><b>Boss redoutable</b></font> apparaît !"),
            RulePage(R.drawable.team_footer, "Gérez votre équipe de <font color='#D84315'><b>6 Pokémon maximum</b></font>. Si l'un de vos Pokémon tombe K.O., vous devrez rapidement en choisir un autre !"),
            RulePage(R.drawable.gold, "Utilisez vos <font color='#D84315'><b>PokéOr</b></font> pour invoquer de nouveaux monstres au Gacha. Récupérer un doublon augmentera de façon permanente ses <font color='#D84315'><b>statistiques</b></font>."),
            RulePage(R.drawable.pokedex_btn, "En montant de niveau, vos Pokémon peuvent <font color='#D84315'><b>évoluer</b></font> pour changer de forme et devenir beaucoup plus puissants. Attrapez-les tous !")
        )

        var currentPage = 0

        // Fonction pour mettre à jour l'affichage selon la page actuelle
        fun updatePage() {
            val page = pages[currentPage]
            imgRule.setImageResource(page.imageRes)

            // Le HtmlCompat permet d'interpréter les balises HTML dans un string Android
            tvRuleText.text = HtmlCompat.fromHtml(page.htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)

            tvPageIndicator.text = "${currentPage + 1} / ${pages.size}"

            // Afficher/Cacher les flèches
            btnPrevRule.visibility = if (currentPage == 0) View.INVISIBLE else View.VISIBLE
            btnNextRule.visibility = if (currentPage == pages.size - 1) View.INVISIBLE else View.VISIBLE
        }

        btnPrevRule.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        btnNextRule.setOnClickListener {
            if (currentPage < pages.size - 1) {
                currentPage++
                updatePage()
            }
        }

        closeRulesBtn.setOnClickListener {
            dialog.dismiss()
            showSettingsDialog(activity) {} // Réouvre les paramètres en quittant
        }

        // On charge la page 1 au démarrage
        updatePage()
        dialog.show()
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