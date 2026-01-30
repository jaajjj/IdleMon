package com.example.idlemon

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonReader(context: Context) {
    val pokemons: String by lazy {
        context.assets.open("pokemon.json").bufferedReader().use { it.readText() }
    }

    val moves: String by lazy {
        context.assets.open("moves.json").bufferedReader().use { it.readText() }
    }

    public fun getFrontSprite(id : Int): String {
        val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/$id.gif"
        return url
    }

    public fun getBackSprite(id : Int): String {
        val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/back/$id.gif"
        return url
    }

    fun creerPokemon(num: Int): Pokemon {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, PokemonSpecies>>() {}.type
        val allSpecies: Map<String, PokemonSpecies> = gson.fromJson(pokemons, typeObjet)
        val foundSpecies = allSpecies.values.find { it.num == num }
        if (foundSpecies != null) {
            Log.i("Pokemon Existe", "Pokemon de num $num trouvé !")
            return Pokemon(foundSpecies)
        } else {
            Log.e("Pokemon Existe pas", "Le pokemon n'existe pas avec le numéro $num")
            throw IllegalArgumentException("Le pokemon n'existe pas avec le numéro $num")
        }
    }

}