package com.example.idlemon

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ModelJson(context: Context) {
    val pokemons: String by lazy {
        context.assets.open("pokemon.json").bufferedReader().use { it.readText() }
    }

    val moves: String by lazy {
        context.assets.open("moves.json").bufferedReader().use { it.readText() }
    }

    fun getFrontSprite(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/$id.gif"
    }

    fun getBackSprite(id : Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/back/$id.gif"
    }

    //Créer Pokemon par ID
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

    //Créer Pokemon par nom
    fun creerPokemon(nom: String): Pokemon {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, PokemonSpecies>>() {}.type
        val allSpecies: Map<String, PokemonSpecies> = gson.fromJson(pokemons, typeObjet)
        val foundSpecies = allSpecies.values.find { it.nom == nom }
        if (foundSpecies != null) {
            Log.i("Pokemon Existe", "Pokemon de nom $nom trouvé !")
            return Pokemon(foundSpecies)
        } else {
            Log.e("Pokemon Existe pas", "Le pokemon n'existe pas avec le nom $nom")
            throw IllegalArgumentException("Le pokemon n'existe pas avec le nom $nom")
        }
    }

    val allAttacks: Map<String, Attack> by lazy {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, Attack>>() {}.type
        gson.fromJson(moves, typeObjet)
    }

    fun getAttackDispo(pokemon: Pokemon): List<Attack> {
        val dispo = mutableListOf<Attack>()
        val pokeTypes = pokemon.species.type.map { it.nom.lowercase() } // On passe en minuscule
        val atackActu = pokemon.attacks.map { it.name }

        for (attack in allAttacks.values) {
            // On compare des Strings
            val attackType = attack.type.lowercase()
            val isCompatible = pokeTypes.contains(attackType) || attackType == "normal"
            val dejaConnu = atackActu.contains(attack.name)

            if (isCompatible && !dejaConnu) {
                dispo.add(attack)
            }
        }
        return dispo
    }

    fun getAttackByNom(nom: String): Attack {
        val trouve = allAttacks[nom]
        if (trouve != null) {
            return trouve
        }
        throw IllegalArgumentException("L'attaque $nom n'existe pas")
    }


    fun getRandomPokemonByRarete(rarete: String): Pokemon {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, PokemonSpecies>>() {}.type
        val tabPoke: Map<String, PokemonSpecies> = gson.fromJson(pokemons, typeObjet)
        val tabFiltre = tabPoke.values.filter { it.rarete == rarete }

        if (tabFiltre.isEmpty()) {
            throw IllegalArgumentException("Aucune espèce de Pokémon avec la rareté $rarete trouvée")
        }
        val randomIndex = (tabFiltre.indices).random()
        val randomSpecies = tabFiltre[randomIndex]
        return Pokemon(randomSpecies)
    }

    fun getRandomPokemon() : Pokemon{
        val randomNum = (1..1000).random()
        when (randomNum) {
            in 1..400 -> return getRandomPokemonByRarete("Commun")
            in 400..700 -> return getRandomPokemonByRarete("Peu commun")
            in 700..890 -> return getRandomPokemonByRarete("Rare")
            in 890..980 -> return getRandomPokemonByRarete("Epique")
            in 980..995 -> return getRandomPokemonByRarete("Fabuleux")
            else -> return getRandomPokemonByRarete("Legendaire")
        }

    }
}