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
import android.widget.Toast

class BattleAttackDialog(
    private val context: Context,
    private val pokemon: Pokemon,
    private val onAttackSelected: (Attack) -> Unit
) {
    private val dialog = Dialog(context)

    fun show() {
        //scrollView pour faire défiler si besoin
        val scrollView = ScrollView(context)
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16, 16, 16, 16)

        container.setBackgroundResource(android.R.color.white)

        scrollView.addView(container)

        //on remplit la liste avec les attaques du Pokémon
        for (attack in pokemon.attacks) {
            val attackView = LayoutInflater.from(context).inflate(R.layout.item_attack_view, container, false)

            val txtNom = attackView.findViewById<TextView>(R.id.nomAttack)
            val txtDesc = attackView.findViewById<TextView>(R.id.descAttack)
            val txtDmg = attackView.findViewById<TextView>(R.id.dmgTextView)
            val txtPp = attackView.findViewById<TextView>(R.id.ppMaxTextView)
            val txtAcc = attackView.findViewById<TextView>(R.id.accTextView)
            val imgType = attackView.findViewById<ImageView>(R.id.CtTypeImg)

            txtNom.text = attack.name
            txtDesc.text = attack.description
            txtDmg.text = attack.basePower.toString()

            //gestion pp
            val attackIndex = pokemon.attacks.indexOf(attack)
            //récup des pp actuelles
            val currentPP = pokemon.currentPP[attackIndex] ?: attack.pp

            txtPp.text = "$currentPP/${attack.pp}"

            if (currentPP == 0) {
                attackView.alpha = 0.5f
            } else {
                attackView.alpha = 1.0f
            }

            txtAcc.text = (attack.accuracy * 100).toInt().toString()
            imgType.setImageResource(DataManager.model.getIconType(attack.type))

            attackView.setOnClickListener {
                if (currentPP == 0) {
                    //Pas assez de PP
                    return@setOnClickListener
                }
                onAttackSelected(attack)
                dialog.dismiss()
            }
            container.addView(attackView)
        }

        dialog.setContentView(scrollView)

        //fond transparent pour le Dialog (pour voir le combat derrière sur les bords)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //taille du dialog (90% largeur, 60% hauteur)
        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.60).toInt()
        dialog.window?.setLayout(width, height)

        dialog.show()
    }
}