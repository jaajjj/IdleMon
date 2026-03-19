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

        //attaque par défaut : 4 attaques random"
        try {
            val attaquesDispo = DataManager.model.getAttackDispo(this)
            attacks.clear()
            val selectedAttacks = attaquesDispo.shuffled().take(4)
            for (atk in selectedAttacks) addAttack(atk)
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
                "item_bague_force" -> baseAtk += (baseAtk * 0.15 * qte).toInt()
                "item_veste_combat" -> baseDef += (baseDef * 0.15 * qte).toInt()
                "item_cape_vitesse" -> baseVit += (baseVit * 0.15 * qte).toInt()
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

    //STAGES DE COMBAT (Buffs / Malus)
    // Paliers allant de -5 à +5
    var stageAtk: Int = 0
    var stageDef: Int = 0
    var stageVit: Int = 0

    // Fonction qui convertit le palier en multiplicateur selon tes règles
    private fun getMultiplicateurStat(stage: Int): Double {
        return when (stage) {
            -5 -> 0.33
            -4 -> 0.50
            -3 -> 0.60
            -2 -> 0.70
            -1 -> 0.80
            0 -> 1.0
            1 -> 2.0
            2 -> 2.5
            3 -> 3.0
            4 -> 3.5
            5 -> 4.0
            else -> if (stage > 0) 4.0 else 0.33
        }
    }

    //stats attack
    // Ce sont ces variables que tu dois utiliser dans tes formules de dégâts !
    val battleAtk: Int
        get() = (currentAtk * getMultiplicateurStat(stageAtk)).toInt()

    val battleDef: Int
        get() = (currentDef * getMultiplicateurStat(stageDef)).toInt()

    val battleVit: Int
        get() = (currentVit * getMultiplicateurStat(stageVit)).toInt()

    //Appliquer buff/malus
    fun modifierStage(stat: String, montant: Int): String {
        var message = ""
        val nomPokemon = species.nom

        when (stat.lowercase()) {
            "atk", "attaque" -> {
                val oldStage = stageAtk
                stageAtk = (stageAtk + montant).coerceIn(-5, 5) // Bloque la valeur entre -5 et 5
                message = genererMessageStat("L'Attaque", oldStage, stageAtk, montant, nomPokemon)
            }
            "def", "defense" -> {
                val oldStage = stageDef
                stageDef = (stageDef + montant).coerceIn(-5, 5)
                message = genererMessageStat("La Défense", oldStage, stageDef, montant, nomPokemon)
            }
            "vit", "vitesse" -> {
                val oldStage = stageVit
                stageVit = (stageVit + montant).coerceIn(-5, 5)
                message = genererMessageStat("La Vitesse", oldStage, stageVit, montant, nomPokemon)
            }
        }
        return message
    }

    private fun genererMessageStat(nomStat: String, oldStage: Int, newStage: Int, montant: Int, nomPoke: String): String {
        if (oldStage == newStage) {
            return if (montant > 0) "$nomStat de $nomPoke ne peut plus augmenter !"
            else "$nomStat de $nomPoke ne peut plus diminuer !"
        }
        return if (montant > 0) {
            if (montant >= 2) "$nomStat de $nomPoke augmente fortement !"
            else "$nomStat de $nomPoke augmente !"
        } else {
            if (montant <= -2) "$nomStat de $nomPoke diminue fortement !"
            else "$nomStat de $nomPoke diminue !"
        }
    }
    fun resetStagesCombat() {
        stageAtk = 0
        stageDef = 0
        stageVit = 0
    }
}