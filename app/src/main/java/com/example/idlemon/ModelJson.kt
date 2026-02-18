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
        val pokeTypes = pokemon.species.type.map { it.nom.lowercase() } // Ex: ["feu", "vol"]
        val atackActu = pokemon.attacks.map { it.name }

        // 1. On récupère les types "étendus" compatibles
        val typesCompatibles = getTypesCompatibles(pokeTypes)

        for (attack in allAttacks.values) {

            // 2. Vérification des attaques Signatures (si tu as mis en place la logique précédente)
            if (attack.exclusive && !isSignatureMoveForPokemon(attack, pokemon)) {
                continue
            }

            val attackType = attack.type.lowercase()
            val isCompatible = typesCompatibles.contains(attackType)
            val dejaConnu = atackActu.contains(attack.name)

            if (isCompatible && !dejaConnu) {
                dispo.add(attack)
            }
        }
        return dispo
    }

    // À la fin de ModelJson.kt

    private fun isSignatureMoveForPokemon(attack: Attack, pokemon: Pokemon): Boolean {
        return when (attack.name) {
            "Feu Sacré" -> pokemon.species.nom == "Ho-Oh" || pokemon.species.nom == "Entei"
            "Aéroblast" -> pokemon.species.nom == "Lugia"
            "Frappe Psy" -> pokemon.species.nom == "Mewtwo"
            "Lame Pangéenne" -> pokemon.species.nom == "Groudon"
            "Onde Originelle" -> pokemon.species.nom == "Kyogre"
            "Draco-Ascension" -> pokemon.species.nom == "Rayquaza"
            "Hurle-Temps" -> pokemon.species.nom == "Dialga"
            "Spatio-Rift" -> pokemon.species.nom == "Palkia"
            "Revenant" -> pokemon.species.nom == "Giratina"
            "Coup Victoire" -> pokemon.species.nom == "Victini"
            "Mort-Ailes" -> pokemon.species.nom == "Yveltal"
            "Géo-Contrôle" -> pokemon.species.nom == "Xerneas"
            "Choc Météore" -> pokemon.species.nom == "Solgaleo"
            "Rayon Spectral" -> pokemon.species.nom == "Lunala"
            "Vitesse Extrême" -> pokemon.species.nom in listOf("Arceus", "Lucario", "Arcanin", "Rayquaza")
            else -> false
        }
    }

    private fun getTypesCompatibles(pokeTypes: List<String>): List<String> {
        val autorisee = mutableSetOf<String>() // Set pour éviter les doublons
        autorisee.add("normal")

        //Ajoute les attaque de ses types
        autorisee.addAll(pokeTypes)

        //ajout des possibilité d'apprentissage selon le type
        for (type in pokeTypes) {
            when (type) {
                "eau" -> autorisee.add("glace")
                "glace" -> autorisee.add("eau")
                "sol" -> autorisee.add("roche")
                "roche" -> autorisee.add("sol")
                "vol" -> autorisee.add("acier")
                "psy" -> {
                    autorisee.add("fee")
                    autorisee.add("spectre")
                }
                "spectre" -> autorisee.add("tenebre")
                "tenebre" -> autorisee.add("spectre")
                "combat" -> autorisee.add("roche")
                "insecte" -> {
                    autorisee.add("poison")
                    autorisee.add("plante")
                }
                "plante" -> autorisee.add("poison")
                "dragon" -> {
                    autorisee.add("feu")
                    autorisee.add("electrik")
                    autorisee.add("glace")
                    autorisee.add("vol")
                }
                "feu" -> autorisee.add("sol")
                "electrik" -> autorisee.add("acier")
                "fee" -> autorisee.add("psy")
            }
        }
        return autorisee.toList()
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