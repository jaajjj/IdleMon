package com.example.idlemon

class Pokemon(val species: PokemonSpecies, var level: Int = 1, var exp: Int = 0) {
    val attacks: MutableList<Attack> = mutableListOf()
    val currentPP: MutableMap<Int, Int> = mutableMapOf()
    var currentHp: Int = species.baseStats.hp
    var currentAtk: Int = species.baseStats.atk
    var currentVit: Int = species.baseStats.vit
    var currentDef: Int = species.baseStats.def
    var isKO: Boolean = false

    init {
        //attack par defaut qui est : Attention, c'est la meilleur des attaques..... CHARGE !!
        addAttack(DataManager.model.getAttackByNom("Charge"))
    }

    fun addAttack(attack: Attack) {
        if (attacks.size < 4) {
            attacks.add(attack)
            currentPP[attacks.size - 1] = attack.pp
        }
    }

    fun replaceAttack(index: Int, newAttack: Attack) {
        if (index in 0 until attacks.size) {
            attacks[index] = newAttack
            currentPP[index] = newAttack.pp
        } else if (index == attacks.size && attacks.size < 4) {
            addAttack(newAttack)
        }
    }

    fun prendreDmg(dmg: Int) {
        if(this.currentHp - dmg > 0){
            this.currentHp -= dmg
        }else{
            this.currentHp = 0
            this.isKO = true
        }
    }

    fun heal(heal: Int) {
        if(this.currentHp + heal < this.species.baseStats.hp){ //a changer
            this.currentHp += heal
        }else{
            this.currentHp = this.species.baseStats.hp
        }
    }

    fun monterLevel() {
        this.level++
        this.currentHp = ((2 * species.baseStats.hp * level) / 100) + level + 10
        this.currentAtk = ((2 * species.baseStats.atk * level) / 100) + 5
        this.currentDef = ((2 * species.baseStats.def * level) / 100) + 5
        this.currentVit = ((2 * species.baseStats.vit * level) / 100) + 5
    }
}