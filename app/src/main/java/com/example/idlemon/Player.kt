package com.example.idlemon

object Player {
    //init Player
    var nom: String = "Sacha"
    private var nbPieces: Int = 10000
    val tabPokemon: MutableList<Pokemon> = mutableListOf()
    val tabEquipe: MutableList<Pokemon> = mutableListOf()

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

    fun addPokemon(pokemon: Pokemon): Boolean {
        //compensation
        if (pokemon in this.tabPokemon || pokemon in this.tabEquipe) {
            addPieces(100)
            return false //déja possédée
        }
        tabPokemon.add(pokemon)
        return true //nouveau poké
    }

    fun removePokemonBox(pokemon: Pokemon) {
        tabPokemon.remove(pokemon)
    }

    fun clearPokemon() {
        tabPokemon.clear()
    }
}