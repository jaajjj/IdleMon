package com.example.idlemon

abstract class Attack(
        val num: Int,
        val name: String,
        val type: PokemonType,
        val description: String,
        val accuracy: Double,
        val pp: Int,
        val basePower: Int = 0,
        val critRatio: Double = 0.04
) {
    var bonus: MutableMap<String, Int> = mutableMapOf()
    var malus: MutableMap<String, Int> = mutableMapOf()

    abstract fun execute(launcher: Pokemon, target: Pokemon)
}

// Attaques de dégats
class DamageAttack(
        num: Int,
        name: String,
        type: PokemonType,
        description: String,
        accuracy: Double,
        pp: Int,
        basePower: Int,
        critRatio: Double
) : Attack(num, name, type, description, accuracy, pp, basePower, critRatio) {

    override fun execute(attacker: Pokemon, defender: Pokemon) {
        var dmg = 0
        val efficacite = type.calculerEfficaciteContre(this.type, defender)
        dmg = (basePower * efficacite).toInt()
        println("$name inflige $dmg dégâts à ${defender.species.nom}")
        defender.prendreDmg(dmg)
    }
}

// Attaque de type Drain (Giga-Sangsue, Vempipoing...)
class DrainAttack(
        num: Int,
        name: String,
        type: PokemonType,
        description: String,
        accuracy: Double,
        pp: Int,
        basePower: Int,
        critRatio: Double
) : Attack(num, name, type, description, accuracy, pp, basePower, critRatio) {

    override fun execute(attacker: Pokemon, defender: Pokemon) {
        // Inflige dégâts ET soigne le lanceur de 20%
        var dmg = 0
        val efficacite = type.calculerEfficaciteContre(this.type, defender)
        dmg = (basePower * efficacite).toInt()
        println("$name inflige $dmg dégâts à ${defender.species.nom}")
        defender.prendreDmg(dmg)
        var heal = (dmg * 0.2).toInt()
        attacker.heal(heal)
    }
}

// Attaque de Statut (Buff ou malus)
class StatusAttack(
        num: Int,
        name: String,
        type: PokemonType,
        description: String,
        accuracy: Double,
        pp: Int
) : Attack(num, name, type, description, accuracy, pp, 0) {

    override fun execute(attacker: Pokemon, defender: Pokemon) {
        // Bonus
        bonus.forEach { (stat, modificateur) ->
            when (stat) {
                "atk" -> attacker.currentAtk += modificateur
                "def" -> attacker.currentDef += modificateur
                "vit" -> attacker.currentVit += modificateur
                "hp" -> attacker.currentHp += modificateur
            }
            println("${defender.species.nom} voit sa stat $stat modifiée de $modificateur")
        }
        malus.forEach { (stat, modificateur) ->
            println("${defender.species.nom} voit sa stat $stat modifiée de $modificateur")
        }
    }
}
