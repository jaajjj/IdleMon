package com.example.idlemon

class Player(
    val nom: String,
    private var nbPieces: Int = 0,
    val tabPokemon: MutableList<Pokemon> = mutableListOf(),
    val tabEquipe: MutableList<Pokemon> = mutableListOf(),
    //val tabObjet: MutableList<Pokemon> = mutableListOf()
) {
    // Méthode pour ajouter un pokémon au tableau
    fun addPokemon(pokemon: Pokemon) {
        tabPokemon.add(pokemon)
    }
     fun getPieces(): Int {
        return nbPieces
    }
    fun addPieces(nbPieces: Int) {
        this.nbPieces += nbPieces
    }
    fun removePieces(nbPieces: Int) {
        this.nbPieces -= nbPieces
    }
    fun getNbPokemon(): Int {
        return tabPokemon.size
    }
    fun getPokemon(index: Int): Pokemon {
        return tabPokemon[index]
    }
    fun getEquipe(): MutableList<Pokemon> {
        return tabEquipe
    }
    fun addEquipe(pokemon: Pokemon) {
        tabEquipe.add(pokemon)
    }
    fun removeEquipe(pokemon: Pokemon) {
        tabEquipe.remove(pokemon)
    }
    fun getNbEquipe(): Int {
        return tabEquipe.size
    }
    fun getPokemonEquipe(index: Int): Pokemon {
        return tabEquipe[index]
    }
}
