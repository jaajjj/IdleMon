package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ChangeAttackDialog(
    private val context: Context,
    private val pokemon: Pokemon
) {
    val dialog = Dialog(context)

    fun show() {
        dialog.setContentView(R.layout.dialog_change_attack)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        closeBtn.setOnClickListener { dialog.dismiss() }

        refreshCurrentAttacks()
        refreshAvailableAttacks()

        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.85).toInt()
        dialog.window?.setLayout(width, height)
    }

    private fun refreshCurrentAttacks() {
        val container = dialog.findViewById<LinearLayout>(R.id.attackContainer)
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for (i in 0 until 4) {
            val layoutId = if (i < pokemon.attacks.size) {
                R.layout.item_attack_view
            } else {
                R.layout.item_attack_vide_view
            }
            val itemView = inflater.inflate(layoutId, container, false)
            itemView.alpha = 1.0f

            if (i < pokemon.attacks.size) {
                fillAttackInfo(itemView, pokemon.attacks[i])
            }
            itemView.setOnClickListener {
                // Logique de sélection (à venir)
            }

            container.addView(itemView)
        }
    }

    private fun refreshAvailableAttacks() {
        val boxLinearLayout = dialog.findViewById<LinearLayout>(R.id.boxLinearLayout)
        boxLinearLayout.removeAllViews()
        val inflater = LayoutInflater.from(context)

        val attacksDispo = DataManager.model.getAttackDispo(pokemon)

        for (atk in attacksDispo) {
            val itemView = inflater.inflate(R.layout.item_attack_view, boxLinearLayout, false)
            itemView.alpha = 1.0f
            fillAttackInfo(itemView, atk)
            boxLinearLayout.addView(itemView)
        }
    }

    private fun fillAttackInfo(view: View, atk: Attack) {
        val ids = listOf(R.id.nomAttack, R.id.descAttack, R.id.dmgTextView, R.id.accTextView, R.id.ppMaxTextView)

        view.findViewById<TextView>(R.id.nomAttack).text = atk.name
        view.findViewById<TextView>(R.id.descAttack).text = atk.description
        view.findViewById<TextView>(R.id.dmgTextView).text = atk.basePower.toString()
        view.findViewById<TextView>(R.id.accTextView).text = (atk.accuracy * 100).toInt().toString()
        view.findViewById<TextView>(R.id.ppMaxTextView).text = atk.pp.toString()

        //correction où le texte noir ne voulait pas apparaitre
        ids.forEach { view.findViewById<TextView>(it).setTextColor(android.graphics.Color.BLACK) }

        view.findViewById<ImageView>(R.id.CtTypeImg).setImageResource(getIconType(atk.type))
    }

    private fun getIconType(typeName: String): Int {
        return when (typeName.replaceFirstChar { it.uppercase() }) {
            "Acier" -> R.drawable.ct_acier
            "Combat" -> R.drawable.ct_combat
            "Dragon" -> R.drawable.ct_dragon
            "Eau" -> R.drawable.ct_eau
            "Feu" -> R.drawable.ct_feu
            "Fee" -> R.drawable.ct_fee
            "Glace" -> R.drawable.ct_glace
            "Insecte" -> R.drawable.ct_insecte
            "Normal" -> R.drawable.ct_normal
            "Plante" -> R.drawable.ct_plante
            "Poison" -> R.drawable.ct_poison
            "Psy" -> R.drawable.ct_psy
            "Roche" -> R.drawable.ct_roche
            "Sol" -> R.drawable.ct_sol
            "Spectre" -> R.drawable.ct_spectre
            "Tenebre" -> R.drawable.ct_tenebre
            "Vol" -> R.drawable.ct_vol
            "Electrik" -> R.drawable.ct_electrik
            else -> R.drawable.ct_normal
        }
    }
}