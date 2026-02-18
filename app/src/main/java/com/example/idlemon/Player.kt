package com.example.idlemon

object Player {
    //init Player
    var nom: String = "Sacha"
    private var nbPieces: Int = 10000
    private val tabPokemon: MutableList<Pokemon> = mutableListOf()
    private val tabEquipe: MutableList<Pokemon> = mutableListOf()

    //pièce
    fun getPieces(): Int = nbPieces

    fun setPieces(nb: Int) {
        this.nbPieces = nb
    }

    fun addPieces(montant: Int) {
        this.nbPieces += montant
    }

    fun removePieces(montant: Int) {
        this.nbPieces -= montant
    }

    //équipe
    fun getEquipe(): MutableList<Pokemon> = tabEquipe

    fun getPremierPokemon(): Pokemon {
        return tabEquipe[0]
    }

    fun addEquipe(pokemon: Pokemon) {
        if (tabEquipe.size < 6) {
            tabEquipe.add(pokemon)
        }
    }

    fun removeEquipe(pokemon: Pokemon) {
        tabEquipe.remove(pokemon)
    }

    fun clearEquipe() {
        tabEquipe.clear()
    }

    //box
    fun getNbPokemon(): Int = tabPokemon.size

    fun getPokemon(index: Int): Pokemon = tabPokemon[index]

    fun aDejaLePokemon(numPokedex: Int): Boolean {
        val dansEquipe = tabEquipe.any { it.species.num == numPokedex }
        val dansBoite = tabPokemon.any { it.species.num == numPokedex }
        return dansEquipe || dansBoite
    }

    fun addPokemon(pokemon: Pokemon): Boolean {
        if (aDejaLePokemon(pokemon.species.num)) {
            addPieces(100)
            return false
        }
        tabPokemon.add(pokemon)
        return true
    }

    fun getBoxPokemon(): MutableList<Pokemon> = tabPokemon

    fun removePokemonBox(pokemon: Pokemon) {
        tabPokemon.remove(pokemon)
    }

    fun clearPokemon() {
        tabPokemon.clear()
    }
}