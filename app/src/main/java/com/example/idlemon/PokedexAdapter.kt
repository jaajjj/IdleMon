package com.example.idlemon

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PokedexAdapter(
    private var listePokemon: List<PokemonSpecies>,
    private val idsPossedes: Set<Int>,
    private val onPokemonClick: (PokemonSpecies, Boolean) -> Unit
) : RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder>() {

    class PokedexViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPokeSprite: ImageView = view.findViewById(R.id.imgPokeSprite)
        val txtPokeNum: TextView = view.findViewById(R.id.txtPokeNum)
        val txtPokeName: TextView = view.findViewById(R.id.txtPokeName)
        val rootLayout: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokedexViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokedex, parent, false)
        return PokedexViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokedexViewHolder, position: Int) {
        val species = listePokemon[position]
        val estPossede = idsPossedes.contains(species.num)

        holder.txtPokeNum.text = "#${species.num.toString().padStart(3, '0')}"

        if (estPossede) {
            holder.txtPokeName.text = species.nom
            holder.imgPokeSprite.clearColorFilter()
            holder.imgPokeSprite.alpha = 1.0f

            // Chargement normal
            Glide.with(holder.itemView.context)
                .load(DataManager.model.getFrontSprite(species.num))
                .into(holder.imgPokeSprite)
        } else {
            holder.txtPokeName.text = "???"

            // Saturation à 0 (Noir et blanc pour faire effet "silhouette")
            val matrix = ColorMatrix().apply { setSaturation(0f) }
            holder.imgPokeSprite.colorFilter = ColorMatrixColorFilter(matrix)
            holder.imgPokeSprite.alpha = 0.5f // Un peu transparent, mais net

            // Chargement normal SANS le flou
            Glide.with(holder.itemView.context)
                .load(DataManager.model.getFrontSprite(species.num))
                .into(holder.imgPokeSprite)
        }

        holder.rootLayout.setOnClickListener {
            onPokemonClick(species, estPossede)
        }
    }

    override fun getItemCount() = listePokemon.size

    // Fonction vitale pour la barre de recherche et les filtres
    fun updateData(nouvelleListe: List<PokemonSpecies>) {
        this.listePokemon = nouvelleListe
        notifyDataSetChanged()
    }
}