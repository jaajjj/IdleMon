package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class BattleAttackDialog(
    private val context: Context,
    private val pokemon: Pokemon,
    private val onAttackSelected: (Attack) -> Unit
) {
    private val dialog = Dialog(context)

    fun show() {
        // ScrollView pour faire défiler si besoin
        val scrollView = ScrollView(context)
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16, 16, 16, 16)

        // Fond blanc pour la liste des attaques
        container.setBackgroundResource(android.R.color.white)

        scrollView.addView(container)

        // On remplit la liste avec les attaques du Pokémon
        for (attack in pokemon.attacks) {
            // On utilise ton layout item_attack_view.xml
            val attackView = LayoutInflater.from(context).inflate(R.layout.item_attack_view, container, false)

            // Récupération des vues
            val txtNom = attackView.findViewById<TextView>(R.id.nomAttack)
            val txtDesc = attackView.findViewById<TextView>(R.id.descAttack)
            val txtDmg = attackView.findViewById<TextView>(R.id.dmgTextView)
            val txtPp = attackView.findViewById<TextView>(R.id.ppMaxTextView)
            val txtAcc = attackView.findViewById<TextView>(R.id.accTextView)
            val imgType = attackView.findViewById<ImageView>(R.id.CtTypeImg)

            // Remplissage des données
            txtNom.text = attack.name
            txtDesc.text = attack.description
            txtDmg.text = attack.basePower.toString()

            // Gestion PP
            val currentPP = pokemon.currentPP[pokemon.attacks.indexOf(attack)] ?: attack.pp
            txtPp.text = "$currentPP/${attack.pp}"

            txtAcc.text = (attack.accuracy * 100).toInt().toString()

            // Image du type
            try {
                imgType.setImageResource(DataManager.model.getIconType(attack.type))
            } catch (e: Exception) {
                // Fallback si erreur
            }

            // Clic sur l'attaque
            attackView.setOnClickListener {
                onAttackSelected(attack)
                dialog.dismiss()
            }

            container.addView(attackView)
        }

        dialog.setContentView(scrollView)

        // Fond transparent pour le Dialog (pour voir le combat derrière sur les bords)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Taille du dialog (90% largeur, 60% hauteur)
        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.60).toInt()
        dialog.window?.setLayout(width, height)

        dialog.show()
    }
}