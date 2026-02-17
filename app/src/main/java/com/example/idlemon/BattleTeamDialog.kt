package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bumptech.glide.Glide

class BattleTeamDialog(
    private val context: Context,
    private val onPokemonSelected: (Pokemon) -> Unit
) {
    private val dialog = Dialog(context)

    fun show() {
        val scrollView = ScrollView(context)
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16, 16, 16, 16)
        // Fond blanc pour la liste
        container.setBackgroundColor(Color.WHITE)
        scrollView.addView(container)

        val equipe = Player.getEquipe()

        for (pokemon in equipe) {
            // J'utilise item_pokemon_team.xml (celui utilisé dans TeamActivity)
            // Assure-toi que ce layout existe bien (il était dans ton prompt précédent)
            val pokeView = LayoutInflater.from(context).inflate(R.layout.item_pokemon_team, container, false)

            val pokeSprite = pokeView.findViewById<ImageView>(R.id.pokeSprite)
            val pokeName = pokeView.findViewById<TextView>(R.id.pokeName)
            val type1 = pokeView.findViewById<ImageView>(R.id.type1)
            val type2 = pokeView.findViewById<ImageView>(R.id.type2)

            // Masquer le bouton d'attaque dans la vue équipe lors d'un combat
            val attackBtn = pokeView.findViewById<ImageView>(R.id.attackBtn1)
            attackBtn.visibility = View.GONE

            // Binding Data
            pokeName.text = pokemon.species.nom
            pokeName.setTextColor(Color.BLACK) // Assurer la visibilité si le fond est blanc

            Glide.with(context)
                .load(DataManager.model.getFrontSprite(pokemon.species.num))
                .into(pokeSprite)

            // Types
            type1.setImageResource(DataManager.model.getIconType(pokemon.species.type[0].nom))
            if (pokemon.species.type.size > 1) {
                type2.visibility = View.VISIBLE
                type2.setImageResource(DataManager.model.getIconType(pokemon.species.type[1].nom))
            } else {
                type2.visibility = View.GONE
            }

            // Gestion du K.O.
            if (pokemon.isKO) {
                pokeView.alpha = 0.5f // Grisé si K.O.
                pokeView.setOnClickListener {
                    // Toast : Impossible d'envoyer un Pokémon KO
                }
            } else {
                pokeView.setOnClickListener {
                    onPokemonSelected(pokemon)
                    dialog.dismiss()
                }
            }

            container.addView(pokeView)
        }

        dialog.setContentView(scrollView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.70).toInt()
        dialog.window?.setLayout(width, height)

        dialog.show()
    }
}