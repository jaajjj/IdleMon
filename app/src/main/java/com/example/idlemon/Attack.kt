package com.example.idlemon

class Attack(
    val num: Int,
    val name: String,
    val type: String,
    val description: String,
    val accuracy: Double,
    val pp: Int,
    val basePower: Int = 0,
    val critRatio: Double = 0.04,
    val drain: Boolean = false,
    val heal: Int = 0,
    val exclusive: Boolean = false
) {
    var bonus: List<Map<String, Int>>? = null
    var malus: List<Map<String, Int>>? = null
}