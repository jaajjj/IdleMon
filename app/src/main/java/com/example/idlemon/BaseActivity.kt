package com.example.idlemon

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

open class BaseActivity : AppCompatActivity() {
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
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // On force la largeur à remplir l'écran (avec une marge via le XML) et la hauteur à s'adapter
            window.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT // ou WRAP_CONTENT si tu préfères que ça ne prenne pas tout l'écran
            )
        }

        val switchMusique = dialog.findViewById<Switch>(R.id.switchMusique)
        val switchDialogue = dialog.findViewById<Switch>(R.id.switchDialogueRapide)
        val creditBtn = dialog.findViewById<AppCompatButton>(R.id.creditBtn)
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)

        //charger l'état actuel
        switchMusique.isChecked = SettingsManager.isMusicEnabled(this)
        switchDialogue.isChecked = SettingsManager.isFastDialogue(this)

        //gestion de la Musique
        switchMusique.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setMusicEnabled(this, isChecked)
            if (isChecked) {
                MusicManager.resume()
            } else {
                //couper la musique
                MusicManager.pause()
            }
        }

        //dialogues rapides
        switchDialogue.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setFastDialogue(this, isChecked)
        }

        //ouvrir les Crédits
        creditBtn.setOnClickListener {
            dialog.dismiss()
            showCreditsDialog()
        }

        //fermer le dialog
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCreditsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_credit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        closeBtn.setOnClickListener {
            dialog.dismiss()
            showSettingsDialog()
        }

        dialog.show()
    }
}