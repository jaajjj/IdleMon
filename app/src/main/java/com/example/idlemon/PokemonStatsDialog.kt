package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class PokemonStatsDialog(private val context: Context) {

    fun show(pokemon: Pokemon) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pokemon_stats)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialisation des vues locales
        val dialogLevel = dialog.findViewById<TextView>(R.id.dialogLevel)
        val dialogRarete = dialog.findViewById<TextView>(R.id.dialogRarete)
        val dialogImage = dialog.findViewById<ImageView>(R.id.dialogImage)
        val dialogName = dialog.findViewById<TextView>(R.id.dialogName)
        val dialogType1 = dialog.findViewById<ImageView>(R.id.dialogType1)
        val dialogType2 = dialog.findViewById<ImageView>(R.id.dialogType2)
        val dialogHp = dialog.findViewById<TextView>(R.id.dialogHp)
        val dialogAtk = dialog.findViewById<TextView>(R.id.dialogAtk)
        val dialogDef = dialog.findViewById<TextView>(R.id.dialogDef)
        val dialogVit = dialog.findViewById<TextView>(R.id.dialogVit)
        val dialogItemsContainer = dialog.findViewById<LinearLayout>(R.id.dialogItemsContainer)
        val dialogPrevo = dialog.findViewById<TextView>(R.id.dialogPrevo)
        val dialogEvo = dialog.findViewById<TextView>(R.id.dialogEvo)
        val dialogBtnClose = dialog.findViewById<ImageView>(R.id.dialogBtnClose)

        dialogBtnClose.setOnClickListener { dialog.dismiss() }

        //remplissage des données
        dialogLevel.text = "Lv. ${pokemon.level}"
        dialogRarete.text = pokemon.species.rarete
        dialogName.text = pokemon.species.nom
        Glide.with(context).load(DataManager.model.getFrontSprite(pokemon.species.num)).into(dialogImage)

        //gestion des types
        if (pokemon.species.type.isNotEmpty()) {
            dialogType1.setImageResource(DataManager.model.getIconType(pokemon.species.type[0].nom))
            dialogType1.visibility = View.VISIBLE
            if (pokemon.species.type.size > 1) {
                dialogType2.visibility = View.VISIBLE
                dialogType2.setImageResource(DataManager.model.getIconType(pokemon.species.type[1].nom))
            } else dialogType2.visibility = View.GONE
        } else {
            dialogType1.visibility = View.GONE
            dialogType2.visibility = View.GONE
        }

        dialogHp.text = "PV : ${pokemon.currentHp}/${pokemon.getMaxHp()}"
        dialogAtk.text = "ATK : ${pokemon.currentAtk}"
        dialogDef.text = "DEF : ${pokemon.currentDef}"
        dialogVit.text = "VIT : ${pokemon.currentVit}"

        //objets équipés
        val objetsEquipes = pokemon.objets.filterValues { it > 0 }
        if (objetsEquipes.isEmpty()) {
            val noItemText = TextView(context).apply {
                text = "Aucun objet équipé"
                setTextColor(Color.DKGRAY)
                textSize = 12f
                gravity = Gravity.CENTER
            }
            dialogItemsContainer.addView(noItemText)
        } else {
            (context as? PlayActivity)?.afficherObjets(pokemon, dialogItemsContainer)
        }

        if (pokemon.species.prevo != null) {
            dialogPrevo.text = "← Évolue depuis : ${pokemon.species.prevo}"
            dialogPrevo.visibility = View.VISIBLE
        } else dialogPrevo.visibility = View.GONE

        if (pokemon.species.evo != null && pokemon.species.evoLevel != null) {
            dialogEvo.text = "→ Évolue en : ${pokemon.species.evo} (Niv. ${pokemon.species.evoLevel})"
        } else dialogEvo.text = "Évolution finale"

        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }
}