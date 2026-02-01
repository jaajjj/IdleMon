package com.example.idlemon

open class Attack(
    val num: Int,
    val name: String,
    val type: String,
    val description: String,
    val accuracy: Double,
    val pp: Int,
    val basePower: Int = 0,
    val critRatio: Double = 0.04,
    val drain: Boolean = false,
    val heal: Int = 0
) {
    var bonus: List<Map<String, Int>>? = null
    var malus: List<Map<String, Int>>? = null

    open fun execute(launcher: Pokemon, target: Pokemon) {
    }
}

class DamageAttack(
    num: Int, name: String, type: String, description: String,
    accuracy: Double, pp: Int, basePower: Int, critRatio: Double
) : Attack(num, name, type, description, accuracy, pp, basePower, critRatio)

class DrainAttack(
    num: Int, name: String, type: String, description: String,
    accuracy: Double, pp: Int, basePower: Int, critRatio: Double
) : Attack(num, name, type, description, accuracy, pp, basePower, critRatio)

class StatusAttack(
    num: Int, name: String, type: String, description: String,
    accuracy: Double, pp: Int
) : Attack(num, name, type, description, accuracy, pp, 0)