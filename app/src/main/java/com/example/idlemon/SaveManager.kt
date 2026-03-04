package com.example.idlemon

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SaveManager {
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private const val NOM_FI_LOCAL = "IdleMonSave"
    private const val CLE_PLAYER_DATA = "player_data"
    
    var isLoaded = false
    var cloudSyncComplete = false // Sécurité pour la synchro multi-appareils

    private fun toInt(value: Any?): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    //save quand on quit
    fun sauvegarderLocal(context: Context) {
        val fichier = context.getSharedPreferences(NOM_FI_LOCAL, Context.MODE_PRIVATE)
        val equipeData = Player.getEquipe().map { serializePokemon(it) }
        val boxData = Player.getBoxPokemon().map { serializePokemon(it) }

        val fullData = mapOf(
            "nbPieces" to Player.getPieces(),
            "equipe" to equipeData,
            "box" to boxData
        )

        fichier.edit { putString(CLE_PLAYER_DATA, Gson().toJson(fullData)) }
    }

    private fun serializePokemon(pokemon: Pokemon): Map<String, Any> {
        return mapOf(
            "num" to pokemon.species.num,
            "level" to pokemon.level,
            "exp" to pokemon.exp,
            "attacks" to pokemon.attacks.map { it.name }
        )
    }

    //chargement unique au lancement
    fun chargerLocal(context: Context) {
        if (isLoaded) return
        
        val json = context.getSharedPreferences(NOM_FI_LOCAL, Context.MODE_PRIVATE).getString(CLE_PLAYER_DATA, null)
        
        if (json == null) {
            resetToDefault(context)
        } else {
            try {
                val data: Map<String, Any> = Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
                reconstruirePlayerDepuisMap(data, context)
            } catch (e: Exception) {
                resetToDefault(context)
            }
        }
        isLoaded = true
    }

    fun resetToDefault(context: Context) {
        Player.setPieces(1000)
        Player.clearEquipe()
        Player.clearPokemon()
        try {
            val pichu = ModelJson(context).creerPokemon(172)
            Player.addEquipe(pichu)
        } catch (e: Exception) {}
        sauvegarderLocal(context)
    }

    private fun reconstruirePlayerDepuisMap(data: Map<String, Any>, context: Context) {
        val modelJson = ModelJson(context)
        Player.setPieces(toInt(data["nbPieces"]))

        Player.clearEquipe()
        (data["equipe"] as? List<Map<String, Any>>)?.forEach { Player.addEquipe(parsePokemon(it, modelJson)) }

        Player.clearPokemon()
        (data["box"] as? List<Map<String, Any>>)?.forEach { Player.addPokemonToBox(parsePokemon(it, modelJson)) }
    }

    private fun parsePokemon(map: Map<String, Any>, modelJson: ModelJson): Pokemon {
        val poke = modelJson.creerPokemon(toInt(map["num"]))
        poke.level = toInt(map["level"])
        poke.exp = toInt(map["exp"])
        (map["attacks"] as? List<String>)?.let { names ->
            poke.attacks.clear()
            names.forEach { poke.attacks.add(modelJson.getAttackByNom(it)) }
        }
        return poke
    }

    //save cloud si login
    fun sauvegarder(onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val user = auth.currentUser ?: return
        
        // SECURITÉ : On ne save pas sur le cloud si on n'a pas encore fini de charger les données existantes
        if (!cloudSyncComplete) return 

        val data = hashMapOf(
            "email" to user.email,
            "nbPieces" to Player.getPieces(),
            "equipe" to Player.getEquipe().map { serializePokemon(it) },
            "box" to Player.getBoxPokemon().map { serializePokemon(it) }
        )
        db.collection("users").document(user.uid).set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Erreur cloud") }
    }

    //synchro cloud si login
    fun charger(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    reconstruirePlayerDepuisMap(doc.data!!, context)
                    isLoaded = true
                    cloudSyncComplete = true // Synchro réussie
                    sauvegarderLocal(context)
                    onSuccess()
                } else {
                    cloudSyncComplete = true // Pas de save cloud, le local devient la référence
                    onFailure()
                }
            }
            .addOnFailureListener { onFailure() }
    }

    fun deleteLocal(context: Context) {
        context.getSharedPreferences(NOM_FI_LOCAL, Context.MODE_PRIVATE).edit { remove(CLE_PLAYER_DATA) }
        isLoaded = false
        cloudSyncComplete = false
    }
}
