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
    val originalSpecies: PokemonSpecies = species

    //INVENTAIRE
    //Key: ID de l'objet (ex: "pv_plus", "restes"), Value: Quantité
    val objets: MutableMap<String, Int> = mutableMapOf()

    //Attaques
    val attacks: MutableList<Attack> = mutableListOf()
    val currentPP: MutableMap<Int, Int> = mutableMapOf()

    //formule XP : (Level^3)
    val xpToNextLevel: Int
        get() = (level + 1).toDouble().pow(3).toInt()

    init {
        //initialisation des stats
        recalculerStats()
        //full HP au début
        currentHp = getMaxHp()

        //attaque par défaut : "Attention, c'est la meilleure... CHARGE !!"
        try {
            addAttack(DataManager.model.getAttackByNom("Charge"))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun ajouterObjet(idObjet: String, quantite: Int = 1) {
        val qteActuelle = objets[idObjet] ?: 0
        objets[idObjet] = qteActuelle + quantite

        //On recalcule les stats au cas où l'objet donne des stats
        recalculerStats()
    }

    fun possedeObjet(idObjet: String): Boolean {
        return (objets[idObjet] ?: 0) > 0
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
        val oldMax = getMaxHp()

        this.level++
        recalculerStats()

        val newMax = getMaxHp()
        this.currentHp += (newMax - oldMax)
    }

    fun recalculerStats() {
        var baseAtk = ((2 * species.baseStats.atk * level) / 100) + 5
        var baseDef = ((2 * species.baseStats.def * level) / 100) + 5
        var baseVit = ((2 * species.baseStats.vit * level) / 100) + 5

        //Application des bonus d'objets
        objets.forEach { (id, qte) ->
            when(id) {
                "atk_plus" -> baseAtk += (5 * qte)
                "def_plus" -> baseDef += (5 * qte)
                "vit_plus" -> baseVit += (5 * qte)
                "atk_plus_plus" -> baseAtk += (10 * qte)
                "def_plus_plus" -> baseDef += (10 * qte)
                "vit_plus_plus" -> baseVit += (10 * qte)
                "item_bague_force" -> baseAtk += (25 * qte)
                "item_veste_combat" -> baseDef += (25 * qte)
                "item_cape_vitesse" -> baseVit += (25 * qte)
            }
        }

        this.currentAtk = baseAtk
        this.currentDef = baseDef
        this.currentVit = baseVit
    }

    //maxHP = ((2 * Base * Lvl) / 100) + Lvl + 10 + BONUS PV
    fun getMaxHp(): Int {
        var maxHp = ((2 * species.baseStats.hp * level) / 100) + level + 10
        objets.forEach { (id, qte) ->
            when(id) {
                "pv_plus" -> maxHp += (10 * qte)
                "pv_plus_plus" -> maxHp += (20 * qte)
            }
        }

        return maxHp
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
        if (this.currentHp > 0) {
            this.isKO = false
        }
    }
}