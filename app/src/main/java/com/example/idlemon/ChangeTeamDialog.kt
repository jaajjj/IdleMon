package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class ChangeTeamDialog(
    private val context: Context,
) {
    val dialog = Dialog(context)
    private var selectedIndex: Int = -1

    private lateinit var imgPokemonChange: ImageView
    private lateinit var txtNomPokemon: TextView

    fun show() {
        dialog.setContentView(R.layout.dialog_change_team)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val window = dialog.window
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            //bg en noir
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.7f)
        }

        imgPokemonChange = dialog.findViewById<ImageView>(R.id.imgPokemonChange)
        txtNomPokemon = dialog.findViewById<TextView>(R.id.NomPokemon)
        txtNomPokemon.text = ""
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        refreshTeamList()
        refreshBoxList()
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.90).toInt()
        dialog.window?.setLayout(width, height)
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
                if (selectedIndex == i) {
                    selectedIndex = -1
                    resetSelectionEffects()
                    imgPokemonChange.setImageDrawable(null)
                    txtNomPokemon.text = ""
                } else {
                    selectedIndex = i
                    resetSelectionEffects()
                    imgView.alpha = 0.5f
                    if (i < equipe.size) {
                        val p = equipe[i]
                        txtNomPokemon.text = p.species.nom
                        Glide.with(context)
                            .asGif()
                            .load(DataManager.model.getFrontSprite(p.species.num))
                            .into(imgPokemonChange)
                    } else {
                        txtNomPokemon.text = "Emplacement vide"
                        imgPokemonChange.setImageResource(R.drawable.pokeball)
                    }
                }
                refreshBoxList()
            }
            imgView.setOnLongClickListener {
                val equipe = Player.getEquipe()
                if (i < equipe.size) {
                    val pokeSupprimer = equipe[i]
                    if (equipe.size > 1) {
                        Toast.makeText(context, "Pokémon supprimé de l'équipe", Toast.LENGTH_SHORT).show()
                        Player.tabPokemon.add(pokeSupprimer)
                        equipe.removeAt(i)
                        selectedIndex = -1
                        imgPokemonChange.setImageDrawable(null)
                        txtNomPokemon.text = ""
                        resetSelectionEffects()
                        refreshTeamList()
                        refreshBoxList()
                    }
                }
                true //arrete le listenenr
            }

            if (i < equipe.size) {
                Glide.with(context)
                    .asGif()
                    .load(DataManager.model.getFrontSprite(equipe[i].species.num))
                    .fitCenter().fitCenter()
                    .into(imgView)
            } else {
                imgView.setImageResource(R.drawable.pokeball)
            }
        }
    }

    private fun refreshBoxList() {
        val boxLinearLayout = dialog.findViewById<LinearLayout>(R.id.boxLinearLayout)
        boxLinearLayout.removeAllViews()
        val inflater = LayoutInflater.from(context)
        val boxPokemons = Player.tabPokemon
        for (pokemon in boxPokemons) {
            val itemView = inflater.inflate(R.layout.item_pokemon_box, boxLinearLayout, false)
            //petit opécité
            itemView.alpha = if(selectedIndex == -1) 0.3f else 1.0f
            val pokeSprite = itemView.findViewById<ImageView>(R.id.pokeSprite)
            val pokeName = itemView.findViewById<TextView>(R.id.pokeName)
            val type1 = itemView.findViewById<ImageView>(R.id.type1)
            val type2 = itemView.findViewById<ImageView>(R.id.type2)

            pokeName.text = pokemon.species.nom
            Glide.with(context)
                .asGif()
                .load(DataManager.model.getFrontSprite(pokemon.species.num))
                .fitCenter() //banger de méthode pour centrer (trop pratique partout. Merci Glide)
                .into(pokeSprite)

            type1.setImageResource(getIconType(pokemon.species.type[0].nom))
            if (pokemon.species.type.size > 1) {
                type2.visibility = View.VISIBLE
                type2.setImageResource(getIconType(pokemon.species.type[1].nom))
            } else {
                type2.visibility = View.GONE
            }

            itemView.setOnClickListener {
                if (selectedIndex != -1) {
                    val equipe = Player.getEquipe()
                    val box = Player.tabPokemon
                    //swap
                    if (selectedIndex < equipe.size) {
                        Toast.makeText(context, "Pokémon échangé", Toast.LENGTH_SHORT).show()
                        val pokemonQuiSort = equipe[selectedIndex]
                        equipe[selectedIndex] = pokemon
                        val indexDansBox = box.indexOf(pokemon)
                        if (indexDansBox != -1) {
                            box[indexDansBox] = pokemonQuiSort
                        }
                    } else {
                        Toast.makeText(context, "Pokémon ajouté à l'équipe", Toast.LENGTH_SHORT).show()
                        Player.addEquipe(pokemon)
                        Player.removePokemonBox(pokemon)
                    }
                    selectedIndex = -1
                    dialog.findViewById<ImageView>(R.id.imgPokemonChange).setImageDrawable(null)
                    txtNomPokemon.text = ""
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
        }
    }

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