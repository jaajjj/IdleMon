package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class ChangeTeamDialog(
    private val context: Context
) {
    //UI
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
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.7f)

            //taille du popup
            val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
            val height = (context.resources.displayMetrics.heightPixels * 0.90).toInt()
            window.setLayout(width, height)
        }

        imgPokemonChange = dialog.findViewById(R.id.imgPokemonChange)
        txtNomPokemon = dialog.findViewById(R.id.NomPokemon)
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)

        //quit et data
        txtNomPokemon.text = ""
        closeBtn.setOnClickListener { dialog.dismiss() }

        //affiche equipe
        refreshTeamList()
        refreshBoxList()
        dialog.show()
    }

    private fun refreshTeamList() {
        val equipe = Player.getEquipe()
        val teamIds = arrayOf(
            R.id.pokeTeam1, R.id.pokeTeam2, R.id.pokeTeam3,
            R.id.pokeTeam4, R.id.pokeTeam5, R.id.pokeTeam6
        )

        for (i in teamIds.indices) {
            val imgView = dialog.findViewById<ImageView>(teamIds[i])

            // Listeners
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

            //supp poke equipe
            imgView.setOnLongClickListener {
                val currentEquipe = Player.getEquipe()
                if (i < currentEquipe.size) {
                    if (currentEquipe.size > 1) {
                        val pokeSupprimer = currentEquipe[i]
                        Toast.makeText(context, "Pokémon supprimé de l'équipe", Toast.LENGTH_SHORT).show()
                        Player.addPokemon(pokeSupprimer)
                        Player.removeEquipe(pokeSupprimer)

                        selectedIndex = -1
                        imgPokemonChange.setImageDrawable(null)
                        txtNomPokemon.text = ""
                        resetSelectionEffects()
                        refreshTeamList()
                        refreshBoxList()
                    } else {
                        Toast.makeText(context, "Vous ne pouvez pas jouer sans pokémon dans l'équipe", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            //gif du haut
            if (i < equipe.size) {
                Glide.with(context)
                    .asGif()
                    .load(DataManager.model.getFrontSprite(equipe[i].species.num))
                    .fitCenter()
                    .into(imgView)
            } else {
                imgView.setImageResource(R.drawable.pokeball)
            }
        }
    }

    private fun refreshBoxList() {
        val boxLinearLayout = dialog.findViewById<LinearLayout>(R.id.boxLinearLayout)
        val inflater = LayoutInflater.from(context)
        val boxPokemons = Player.getBoxPokemon()

        boxLinearLayout.removeAllViews()

        for (pokemon in boxPokemons) {
            val itemView = inflater.inflate(R.layout.item_pokemon_box, boxLinearLayout, false)
            val pokeSprite = itemView.findViewById<ImageView>(R.id.obj1)
            val pokeName = itemView.findViewById<TextView>(R.id.pokeName)
            val type1 = itemView.findViewById<ImageView>(R.id.type1)
            val type2 = itemView.findViewById<ImageView>(R.id.type2)

            itemView.alpha = if (selectedIndex == -1) 0.3f else 1.0f
            pokeName.text = pokemon.species.nom

            Glide.with(context)
                .asGif()
                .load(DataManager.model.getFrontSprite(pokemon.species.num))
                .fitCenter()
                .into(pokeSprite)

            //1er type
            type1.setImageResource(DataManager.model.getIconType(pokemon.species.type[0].nom))
            //2eme type si il y a
            if (pokemon.species.type.size > 1) {
                type2.visibility = View.VISIBLE
                type2.setImageResource(DataManager.model.getIconType(pokemon.species.type[1].nom))
            } else {
                type2.visibility = View.GONE
            }

            //listner swap ou add equipe
            itemView.setOnClickListener {
                if (selectedIndex != -1) {
                    val equipe = Player.getEquipe()
                    val box = Player.getBoxPokemon()

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
                    imgPokemonChange.setImageDrawable(null)
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
            dialog.findViewById<ImageView>(id).alpha = 1.0f
        }
    }
}