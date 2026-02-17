package com.example.idlemon

import kotlin.math.pow

class Pokemon(
    var species: PokemonSpecies,
    var level: Int = 1,
    var exp: Int = 0
) {
    //Stats
    var currentHp: Int = 0
    var currentAtk: Int = 0
    var currentDef: Int = 0
    var currentVit: Int = 0
    var isKO: Boolean = false

    //Attaques
    val attacks: MutableList<Attack> = mutableListOf()
    val currentPP: MutableMap<Int, Int> = mutableMapOf()

    // XP : (Level^3)
    val xpToNextLevel: Int
        get() = (level + 1).toDouble().pow(3).toInt()

    init {
        //initialisation des stats
        recalculerStats()
        //full HP au début
        currentHp = getMaxHp()

        //attaque par défaut : "Attention, c'est la meilleure... CHARGE !!"
        addAttack(DataManager.model.getAttackByNom("Charge"))
    }

    //retourne true si le Pokémon a monté de niveau
    fun gagnerExperience(amount: Int): Boolean {
        this.exp += amount
        var aLevelUp = false

        //boucle au cas où on gagne assez d'XP pour prendre plusieurs niveaux d'un coup
        while (this.exp >= xpToNextLevel) {
            this.exp -= xpToNextLevel
            monterLevel()
            aLevelUp = true
        }
        return aLevelUp
    }

    //level
    fun monterLevel() {
        this.level++
        recalculerStats()
        val oldMax = currentHp
        val newMax = getMaxHp()

        this.currentHp += (newMax - getMaxHp(level - 1))
    }

    private fun recalculerStats() {
        // Stats : ((2 * Base * Lvl) / 100) + 5
        this.currentAtk = ((2 * species.baseStats.atk * level) / 100) + 5
        this.currentDef = ((2 * species.baseStats.def * level) / 100) + 5
        this.currentVit = ((2 * species.baseStats.vit * level) / 100) + 5
    }

    //maxHP = ((2 * Base * Lvl) / 100) + Lvl + 10
    fun getMaxHp(): Int {
        return ((2 * species.baseStats.hp * level) / 100) + level + 10
    }

    fun getMaxHp(lvl: Int): Int {
        return ((2 * species.baseStats.hp * lvl) / 100) + lvl + 10
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

    //combat
    fun prendreDmg(dmg: Int) {
        if (this.currentHp - dmg > 0) {
            this.currentHp -= dmg
        } else {
            this.currentHp = 0
            this.isKO = true
        }
    }

    fun heal(heal: Int) {
        val maxHp = getMaxHp()
        if (this.currentHp + heal < maxHp) {
            this.currentHp += heal
        } else {
            this.currentHp = maxHp
        }
        // Si on soigne, on n'est plus KO
        if (this.currentHp > 0) {
            this.isKO = false
        }
    }
}