package com.example.idlemon

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ... le reste de ta classe est correct
open class BaseActivity : AppCompatActivity() {

    // Initialisation simplifiée de Firebase
    protected val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    protected val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    override fun onStart() {
        super.onStart()
        MusicManager.onStartActivity()
    }

    override fun onStop() {
        super.onStop()
        MusicManager.onStopActivity()
    }

    fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val window = dialog.window
        if (window != null) {
            window.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val switchMusique = dialog.findViewById<Switch>(R.id.switchMusique)
        val switchDialogue = dialog.findViewById<Switch>(R.id.switchDialogueRapide)
        val creditBtn = dialog.findViewById<AppCompatButton>(R.id.creditBtn)
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        val textCreateAccount = dialog.findViewById<TextView>(R.id.tvCreateAccount)

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

        textCreateAccount.setOnClickListener {
            dialog.dismiss()
            showRegisterDialog()
        }

        closeBtn.setOnClickListener { dialog.dismiss() }
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

    private fun showRegisterDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_register)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val labEmail = dialog.findViewById<EditText>(R.id.etRegEmail)
        val labPass = dialog.findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = dialog.findViewById<AppCompatButton>(R.id.btnRegisterSubmit)

        dialog.findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
            dialog.dismiss()
            showSettingsDialog()
        }

        btnRegister.setOnClickListener {
            val email = labEmail.text.toString().trim()
            val password = labPass.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 6) {
                // Utilisation de l'instance simplifiée
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                savePlayerDataToFirestore(userId, email, dialog)
                            }
                        } else {
                            Toast.makeText(this, "Erreur: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Données invalides (Pass: min 6 car.)", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun savePlayerDataToFirestore(userId: String, email: String, dialog: Dialog) {
        // Préparation des données (Conversion de vos objets Player en Map pour Firestore)
        val equipeSave = Player.getEquipe().map { pokemon ->
            mapOf("num" to pokemon.species.num, "level" to pokemon.level, "exp" to pokemon.exp)
        }

        val boxData = Player.getBoxPokemon().map { pokemon ->
            mapOf("num" to pokemon.species.num, "level" to pokemon.level, "exp" to pokemon.exp)
        }

        val userSave = hashMapOf(
            "email" to email,
            "nbPieces" to Player.getPieces(),
            "equipe" to equipeSave,
            "box" to boxData
        )

        db.collection("users").document(userId)
            .set(userSave)
            .addOnSuccessListener {
                Toast.makeText(this, "Compte créé et partie synchronisée !", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur Cloud: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}