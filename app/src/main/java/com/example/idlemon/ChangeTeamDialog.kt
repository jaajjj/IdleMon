package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class ChangeTeamDialog(
    private val context: Context,
) {
    val dialog = Dialog(context)
    private var selectedIndex: Int = -1

    fun show() {
        dialog.setContentView(R.layout.dialog_change_team)

        //popup en transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        //on enleve les barre du system
        val window = dialog.window
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            //ajoute le bg avec les ombres
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.7f)
        }

        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        refreshTeamList()
        refreshBoxList()

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun refreshTeamList() {
        val equipe = Player.getEquipe()
        val teamIds = arrayOf(
            R.id.pokeTeam1, R.id.pokeTeam2, R.id.pokeTeam3,
            R.id.pokeTeam4, R.id.pokeTeam5, R.id.pokeTeam6
        )

        for (i in teamIds.indices) {
            val imgView = dialog.findViewById<ImageView>(teamIds[i])
            imgView.setOnClickListener {
                //on annule la selec
                if (selectedIndex == i) {
                    selectedIndex = -1
                    resetSelectionEffects()
                } else { //on selec
                    selectedIndex = i
                    resetSelectionEffects()
                    imgView.alpha = 0.5f
                }
                refreshBoxList()
            }
            if (i < equipe.size) {
                Glide.with(context)
                    .asGif()
                    .load(DataManager.model.getFrontSprite(equipe[i].species.num))
                    .into(imgView)
            } else {
                imgView.setImageResource(R.drawable.pokeball)
            }
        }
    }

    private fun refreshBoxList() {
        val boxLinearLayout = dialog.findViewById<LinearLayout>(R.id.boxLinearLayout)
        boxLinearLayout.removeAllViews() //vidage

        val inflater = LayoutInflater.from(context)
        val boxPokemons = Player.tabPokemon


        for (pokemon in boxPokemons) {
            val itemView = inflater.inflate(R.layout.item_pokemon_box, boxLinearLayout, false)
            if(selectedIndex == -1){
                itemView.alpha = 0.5f
            }else{
                itemView.alpha = 1.0f
            }
            val pokeSprite = itemView.findViewById<ImageView>(R.id.pokeSprite)
            val pokeName = itemView.findViewById<TextView>(R.id.pokeName)
            val type1 = itemView.findViewById<ImageView>(R.id.type1)
            val type2 = itemView.findViewById<ImageView>(R.id.type2)

            //remplissage
            pokeName.text = pokemon.species.nom

            Glide.with(context)
                .asGif()
                .load(DataManager.model.getFrontSprite(pokemon.species.num))
                .into(pokeSprite)

            type1.setImageResource(getIconType(pokemon.species.type[0].nom))
            if (pokemon.species.type.size > 1) {
                type2.visibility = View.VISIBLE
                type2.setImageResource(getIconType(pokemon.species.type[1].nom))
            } else {
                type2.visibility = View.GONE
            }

            //Swap
            itemView.setOnClickListener {
                if (selectedIndex != -1) {
                    val equipe = Player.getEquipe()
                    val box = Player.tabPokemon
                    if (selectedIndex < equipe.size) {
                        val pokemonQuiSort = equipe[selectedIndex]
                        equipe[selectedIndex] = pokemon
                        val indexDansBox = box.indexOf(pokemon)
                        if (indexDansBox != -1) {
                            box[indexDansBox] = pokemonQuiSort
                        }
                    } else {
                        Player.addEquipe(pokemon) //slot vite (pokeball)
                        Player.removePokemonBox(pokemon)
                    }
                    selectedIndex = -1
                    resetSelectionEffects()
                    refreshTeamList()
                    refreshBoxList()
                }
            }
            boxLinearLayout.addView(itemView)
        }
    }

    private fun resetSelectionEffects() {
        val teamIds = arrayOf(
            R.id.pokeTeam1, R.id.pokeTeam2, R.id.pokeTeam3,
            R.id.pokeTeam4, R.id.pokeTeam5, R.id.pokeTeam6
        )

        for (id in teamIds) {
            val imgView = dialog.findViewById<ImageView>(id)
            imgView.alpha = 1.0f
            imgView.setBackgroundResource(0)
        }
    }

    //Je sais que j'aurais pu mettre la fonction getIconType de TeamActivity en public, mais j'ai par réussi. Donc j'ai recopié la fonction...
    private fun getIconType(typeName: String): Int {
        return when (typeName) {
            "Acier" -> R.drawable.acier
            "Combat" -> R.drawable.combat
            "Dragon" -> R.drawable.dragon
            "Eau" -> R.drawable.eau
            "Feu" -> R.drawable.feu
            "Fee" -> R.drawable.fee
            "Glace" -> R.drawable.glace
            "Insecte" -> R.drawable.insecte
            "Normal" -> R.drawable.normal
            "Plante" -> R.drawable.plante
            "Poison" -> R.drawable.poison
            "Psy" -> R.drawable.psy
            "Roche" -> R.drawable.roche
            "Sol" -> R.drawable.sol
            "Spectre" -> R.drawable.spectre
            "Tenebre" -> R.drawable.tenebre
            "Vol" -> R.drawable.vol
            "Electrik" -> R.drawable.electrik
            else -> R.drawable.normal
        }
    }
}