package com.example.idlemon

data class PokemonSpecies(
        val num: Int,
        val nom: String,
        val types: List<PokemonType>,
        val rarete: String,
        val baseStats: Stats,
        val prevo: String?,
        val evo: String?,
        val evoLevel: Int?,
)
