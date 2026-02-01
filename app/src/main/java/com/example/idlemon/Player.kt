package com.example.idlemon

//1 seul Player
object Player {
    var nom: String = "Sacha"
    private var nbPieces: Int = 1000
    val tabPokemon: MutableList<Pokemon> = mutableListOf()
    val tabEquipe: MutableList<Pokemon> = mutableListOf()

    fun addPokemon(pokemon: Pokemon) {
        tabPokemon.add(pokemon)
    }

    fun getPieces(): Int = nbPieces

    fun addPieces(montant: Int) {
        this.nbPieces += montant
    }

    fun removePieces(montant: Int) {
        this.nbPieces -= montant
    }

    fun getNbPokemon(): Int = tabPokemon.size

    fun getPokemon(index: Int): Pokemon = tabPokemon[index]

    fun getEquipe(): MutableList<Pokemon> = tabEquipe

    fun addEquipe(pokemon: Pokemon) {
        if (tabEquipe.size < 6) {
            tabEquipe.add(pokemon)
        }
    }

    fun removeEquipe(pokemon: Pokemon) {
        tabEquipe.remove(pokemon)
    }

    fun getPremierPokemon() : Pokemon{
        return tabEquipe[0]
    }

    fun clearEquipe() {
        tabEquipe.clear()
    }

    fun clearPokemon(){
        tabPokemon.clear()
    }

    fun removePokemonBox(pokemon: Pokemon){
        tabPokemon.remove(pokemon)
    }
}
