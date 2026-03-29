package com.example.idlemon

import android.graphics.Color

object UIHelper {
    fun getColorForType(type: String): Int {
        return when (type.lowercase()) {
            "feu" -> Color.parseColor("#EE8130")
            "eau" -> Color.parseColor("#6390F0")
            "plante" -> Color.parseColor("#7AC74C")
            "électrik", "electrik" -> Color.parseColor("#F7D02C")
            "glace" -> Color.parseColor("#96D9D6")
            "combat" -> Color.parseColor("#C22E28")
            "poison" -> Color.parseColor("#A33EA1")
            "sol" -> Color.parseColor("#E2BF65")
            "vol" -> Color.parseColor("#A98FF3")
            "psy" -> Color.parseColor("#F95587")
            "insecte" -> Color.parseColor("#A6B91A")
            "roche" -> Color.parseColor("#B6A136")
            "spectre" -> Color.parseColor("#735797")
            "dragon" -> Color.parseColor("#6F35FC")
            "ténèbres", "tenebres" -> Color.parseColor("#705848")
            "acier" -> Color.parseColor("#B7B7CE")
            "fée", "fee" -> Color.parseColor("#D685AD")
            else -> Color.parseColor("#A8A77A")
        }
    }

    fun getIconForObjet(id: String): Int {
        return when (id) {
            "atk_plus", "atk_plus_plus" -> R.drawable.attaque_plus
            "def_plus", "def_plus_plus" -> R.drawable.def_plus
            "vit_plus", "vit_plus_plus" -> R.drawable.vit_plus
            "pv_plus", "pv_plus_plus" -> R.drawable.pv_plus
            "item_restes" -> R.drawable.restes
            "item_bague_force" -> R.drawable.bague_force
            "item_veste_combat" -> R.drawable.veste_combat
            "item_cape_vitesse" -> R.drawable.cape_vitesse
            else -> R.drawable.pokeball
        }
    }
}