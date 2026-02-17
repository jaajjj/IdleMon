package com.example.idlemon

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ModelJson(context: Context) {

    // --- Data & Lazy Loading ---
    val pokemons: String by lazy {
        context.assets.open("pokemon.json").bufferedReader().use { it.readText() }
    }

    val moves: String by lazy {
        context.assets.open("moves.json").bufferedReader().use { it.readText() }
    }

    val allAttacks: Map<String, Attack> by lazy {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, Attack>>() {}.type
        gson.fromJson(moves, typeObjet)
    }

    // --- Sprites ---
    fun getFrontSprite(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/$id.gif"
    }

    fun getBackSprite(id : Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/back/$id.gif"
    }

    // --- Gestion des icônes
    fun getIconType(typeName: String): Int {
        return when (typeName.replaceFirstChar { it.uppercase() }) {
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

    // --- Création de Pokémon ---
    fun creerPokemon(num: Int): Pokemon {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, PokemonSpecies>>() {}.type
        val allSpecies: Map<String, PokemonSpecies> = gson.fromJson(pokemons, typeObjet)
        val foundSpecies = allSpecies.values.find { it.num == num }

        return if (foundSpecies != null) {
            Log.i("Pokemon Existe", "Pokemon de num $num trouvé !")
            Pokemon(foundSpecies)
        } else {
            Log.e("Pokemon Existe pas", "Le pokemon n'existe pas avec le numéro $num")
            throw IllegalArgumentException("Le pokemon n'existe pas avec le numéro $num")
        }
    }

    fun creerPokemon(nom: String?): Pokemon {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, PokemonSpecies>>() {}.type
        val allSpecies: Map<String, PokemonSpecies> = gson.fromJson(pokemons, typeObjet)
        val foundSpecies = allSpecies.values.find { it.nom == nom }

        return if (foundSpecies != null) {
            Log.i("Pokemon Existe", "Pokemon de nom $nom trouvé !")
            Pokemon(foundSpecies)
        } else {
            Log.e("Pokemon Existe pas", "Le pokemon n'existe pas avec le nom $nom")
            throw IllegalArgumentException("Le pokemon n'existe pas avec le nom $nom")
        }
    }

    // --- Logique métier (Attaques & Gacha) ---
    fun getAttackDispo(pokemon: Pokemon): List<Attack> {
        val dispo = mutableListOf<Attack>()
        val pokeTypes = pokemon.species.type.map { it.nom.lowercase() }
        val atackActu = pokemon.attacks.map { it.name }

        for (attack in allAttacks.values) {
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
        return allAttacks[nom] ?: throw IllegalArgumentException("L'attaque $nom n'existe pas")
    }

    fun getRandomPokemonByRarete(rarete: String): Pokemon {
        val gson = Gson()
        val typeObjet = object : TypeToken<Map<String, PokemonSpecies>>() {}.type
        val tabPoke: Map<String, PokemonSpecies> = gson.fromJson(pokemons, typeObjet)
        val tabFiltre = tabPoke.values.filter { it.rarete == rarete }

        if (tabFiltre.isEmpty()) {
            throw IllegalArgumentException("Aucune espèce de Pokémon avec la rareté $rarete trouvée")
        }
        return Pokemon(tabFiltre.random())
    }

    fun getRandomPokemon() : Pokemon {
        val randomNum = (1..1000).random()
        return when (randomNum) {
            in 1..399 -> getRandomPokemonByRarete("Commun")
            in 400..699 -> getRandomPokemonByRarete("Peu commun")
            in 700..889 -> getRandomPokemonByRarete("Rare")
            in 890..979 -> getRandomPokemonByRarete("Epique")
            in 980..995 -> getRandomPokemonByRarete("Fabuleux")
            else -> getRandomPokemonByRarete("Legendaire")
        }
    }
}