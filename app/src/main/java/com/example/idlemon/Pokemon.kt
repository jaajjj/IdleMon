package com.example.idlemon

class Pokemon(
    val species: PokemonSpecies,
    var level: Int = 1,
    var exp: Int = 0
) {
    //stats
    var currentHp: Int = species.baseStats.hp
    var currentAtk: Int = species.baseStats.atk
    var currentDef: Int = species.baseStats.def
    var currentVit: Int = species.baseStats.vit
    var isKO: Boolean = false

    //attaques
    val attacks: MutableList<Attack> = mutableListOf()
    val currentPP: MutableMap<Int, Int> = mutableMapOf()

    init {
        // Attaque par défaut : "Attention, c'est la meilleure... CHARGE !!"
        addAttack(DataManager.model.getAttackByNom("Charge"))
    }

    //attaques
    fun addAttack(attack: Attack) {
        if (attacks.size < 4) {
            attacks.add(attack)
            currentPP[attacks.size - 1] = attack.pp
        }
    }

    fun replaceAttack(index: Int, newAttack: Attack) {
        if (index < attacks.size) {
            attacks[index] = newAttack
            currentPP[index] = newAttack.pp
        } else {
            addAttack(newAttack)
        }
    }

    //Combat
    fun prendreDmg(dmg: Int) {
        if (this.currentHp - dmg > 0) {
            this.currentHp -= dmg
        } else {
            this.currentHp = 0
            this.isKO = true
        }
    }

    fun heal(heal: Int) {
        val maxHp = this.species.baseStats.hp/2
        if (this.currentHp + heal < maxHp) {
            this.currentHp += heal
        } else {
            this.currentHp = maxHp
        }
    }

    //level
    fun monterLevel() {
        this.level++

        //stats
        this.currentHp = ((2 * species.baseStats.hp * level) / 100) + level + 10
        this.currentAtk = ((2 * species.baseStats.atk * level) / 100) + 5
        this.currentDef = ((2 * species.baseStats.def * level) / 100) + 5
        this.currentVit = ((2 * species.baseStats.vit * level) / 100) + 5
    }
}