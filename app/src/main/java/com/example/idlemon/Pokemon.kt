package com.example.idlemon

class Pokemon(val species: PokemonSpecies, var level: Int = 1, var exp: Int = 0) {
    val attacks: MutableList<Attack> = mutableListOf()

    var currentHp: Int = species.baseStats.hp
    var currentAtk: Int = species.baseStats.atk
    var currentVit: Int = species.baseStats.vit
    var currentDef: Int = species.baseStats.def

    var isKO: Boolean = false

    fun performAttack(attack: Attack){
        //TODO
    }




    fun prendreDmg(dmg: Int) {
        if (this.currentHp - dmg < 0) {
            this.currentHp = 0
            // KO
            this.isKO = true
            println("${this.species.nom} est KO")
        } else {
            this.currentHp -= dmg
        }
    }

    fun heal(heal: Int) {
        if (this.currentHp + heal > this.species.baseStats.hp) {
            this.currentHp = this.species.baseStats.hp
        } else {
            this.currentHp += heal
        }
    }

    fun monterLevel() {
        this.level++
        this.currentHp = ((2 * species.baseStats.hp * level) / 100) + level + 10
        this.currentAtk = ((2 * species.baseStats.atk * level) / 100) + 5
        this.currentDef = ((2 * species.baseStats.def * level) / 100) + 5
        this.currentVit = ((2 * species.baseStats.vit * level) / 100) + 5
    }

    fun chkKo():Boolean{
        if(this.currentHp <= 0) return true
        return false
    }


}
