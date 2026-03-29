package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import kotlin.random.Random

class RewardBattleVague(
    private val context: Context,
    private val activePokemon: Pokemon,
    private val onRewardSelected: (List<String>) -> Unit
) {
    val dialog = Dialog(context)

    enum class Rarity(val color: Int) {
        COMMON(Color.BLACK),
        EPIC(Color.parseColor("#9C27B0")),
        LEGENDARY(Color.BLUE)
    }

    data class Reward(
        val id: String,
        val name: String,
        val description: String,
        val imageRes: Int,
        val rarity: Rarity,
        val weight: Int,
        val isTargeted: Boolean = true,
        val action: (Pokemon) -> String
    )

    private val availableRewards = listOf(
        //COMMUNS
        Reward("lvl_1", "Bonbon", "+1 Niveau", R.drawable.bonbon, Rarity.COMMON, 50, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.monterLevel()
            "${poke.species.nom} mange un Bonbon et gagne 1 niveau !"
        },
        Reward("atk_1", "Attack +", "+5 Attaque (Perm.)", R.drawable.attaque_plus, Rarity.COMMON, 40, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("atk_plus")
            "L'Attaque de ${poke.species.nom} augmente de 5 !"
        },
        Reward("pv_1", "PV +", "+5 PV Max (Perm.)", R.drawable.pv_plus, Rarity.COMMON, 40, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("pv_plus")
            poke.heal(5)
            "Les PV Max de ${poke.species.nom} augmentent de 5 !"
        },
        Reward("def_1", "Defense +", "+5 Défense (Perm.)", R.drawable.def_plus, Rarity.COMMON, 40, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("def_plus")
            "La Défense de ${poke.species.nom} augmente de 5 !"
        },
        Reward("vit_1", "Vitesse +", "+5 Vitesse (Perm.)", R.drawable.vit_plus, Rarity.COMMON, 40, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("vit_plus")
            "La Vitesse de ${poke.species.nom} augmente de 5 !"
        },
        Reward("heal_50", "Soin", "Soin 50%", R.drawable.potion, Rarity.COMMON, 60, true) { poke ->
            MusicManager.jouerSonBattle("heal")
            val amount = (poke.getMaxHp() * 0.5).toInt()
            poke.heal(amount)
            "${poke.species.nom} récupère 50% de ses PV !"
        },

        //ÉPIQUES
        Reward("lvl_3", "Super Bonbon", "+2 Niveaux", R.drawable.super_bonbon, Rarity.EPIC, 15, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            repeat(2) { poke.monterLevel() }
            "${poke.species.nom} engloutit un Super Bonbon et gagne 2 niveaux !"
        },
        Reward("atk_2", "Attack ++", "+8 Attaque (Perm.)", R.drawable.attaque_plus, Rarity.EPIC, 10, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("atk_plus_plus")
            "L'Attaque de ${poke.species.nom} augmente fortement (+8) !"
        },
        Reward("pv_2", "PV ++", "+8 PV Max (Perm.)", R.drawable.pv_plus, Rarity.EPIC, 10, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("pv_plus_plus")
            poke.heal(8)
            "Les PV Max de ${poke.species.nom} augmentent fortement (+8) !"
        },
        Reward("def_2", "Defense ++", "+8 Défense (Perm.)", R.drawable.def_plus, Rarity.EPIC, 10, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("def_plus_plus")
            "La Défense de ${poke.species.nom} augmente fortement (+8) !"
        },
        Reward("vit_2", "Vitesse ++", "+8 Vitesse (Perm.)", R.drawable.vit_plus, Rarity.EPIC, 10, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("vit_plus_plus")
            "La Vitesse de ${poke.species.nom} augmente fortement (+8) !"
        },
        Reward("heal_100", "Super Soin", "Soin 100%", R.drawable.super_potion, Rarity.EPIC, 25, true) { poke ->
            MusicManager.jouerSonBattle("heal")
            poke.heal(poke.getMaxHp())
            "${poke.species.nom} est entièrement soigné !"
        },

        //LÉGENDAIRES (Mix d'effets de zone et d'équipements)
        Reward("heal_team", "Hyper Soin", "Soin Équipe 100%", R.drawable.hyper_potion, Rarity.LEGENDARY, 5, false) { _ ->
            MusicManager.jouerSonBattle("heal")
            Player.getEquipe().forEach { it.heal(it.getMaxHp()) }
            "Toute l'équipe est entièrement soignée !"
        },
        Reward("rappel_max", "Rappel Max", "Réanime toute l'équipe (50%)", R.drawable.rappel_max, Rarity.LEGENDARY, 5, false) { _ ->
            MusicManager.jouerSonBattle("heal")
            var count = 0
            Player.getEquipe().forEach {
                if (it.isKO) {
                    it.isKO = false
                    it.currentHp = it.getMaxHp() / 2
                    count++
                }
            }
            if(count > 0) {
                MusicManager.jouerSonBattle("heal")
                "$count Pokémon réanimés !"
            } else "Aucun Pokémon n'était K.O."
        },
        Reward("gold", "PokéGold", "Or aléatoire (50-300)", R.drawable.gold, Rarity.LEGENDARY, 35, false) { _ ->
            MusicManager.jouerSonBattle("item_active")
            val amount = Random.nextInt(50, 30)
            if (context is PlayActivity) context.ajouterOr(amount)
            Player.addPieces(amount)
            "Vous trouvez $amount PokéGold supplémentaires !"
        },
        Reward("item_restes", "Restes", "Soigne à chaque tour", R.drawable.restes, Rarity.LEGENDARY, 5, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("item_restes")
            "${poke.species.nom} s'équipe de Restes !"
        },
        Reward("item_bague_force", "Bague Force", "+25 Attaque", R.drawable.bague_force, Rarity.LEGENDARY, 5, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("item_bague_force")
            "L'Attaque de ${poke.species.nom} explose (+15) !"
        },
        Reward("item_veste_combat", "Veste de Combat", "+25 defense", R.drawable.veste_combat, Rarity.LEGENDARY, 5, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("item_veste_combat")
            "La Défense de ${poke.species.nom} explose (+15) !"
        },
        Reward("item_cape_vitesse", "Cape Vitesse", "+25 vitesse", R.drawable.cape_vitesse, Rarity.LEGENDARY, 5, true) { poke ->
            MusicManager.jouerSonBattle("item_active")
            poke.ajouterObjet("item_cape_vitesse")
            "La Vitesse de ${poke.species.nom} explose (+15) !"
        }
    )

    fun show() {
        dialog.setContentView(R.layout.vue_objet_battle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val window = dialog.window
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.7f)
            val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
            val height = (context.resources.displayMetrics.heightPixels * 0.60).toInt()
            window.setLayout(width, height)
        }

        val selectedRewards = pickRandomRewards(3)

        setupRewardView(selectedRewards[0], R.id.containerObj1, R.id.imgObj1, R.id.nomObj1, R.id.btnObj1)
        setupRewardView(selectedRewards[1], R.id.containerObj2, R.id.imgObj2, R.id.nomObj2, R.id.btnObj2)
        setupRewardView(selectedRewards[2], R.id.containerObj3, R.id.imgObj3, R.id.nomObj3, R.id.btnObj3)

        dialog.show()
    }

    private fun setupRewardView(reward: Reward, containerId: Int, imgId: Int, txtId: Int, btnId: Int) {
        val container = dialog.findViewById<ConstraintLayout>(containerId)
        val img = dialog.findViewById<ImageView>(imgId)
        val txt = dialog.findViewById<TextView>(txtId)
        val btn = dialog.findViewById<TextView>(btnId)

        txt.text = reward.name
        txt.setTextColor(reward.rarity.color)

        Glide.with(context).load(reward.imageRes).into(img)

        //si la récompense cible un Pokémon en particulier --> on demande à "Choisir"
        btn.text = if (reward.isTargeted) "Donner" else "Récupérer"

        container.setOnClickListener {
            if (reward.isTargeted && context is PlayActivity) {
                dialog.dismiss()
                // On réutilise la fonction de PlayActivity avec le mode "sélection d'objet" à true
                context.afficherMenuEquipe(etaitKo = false, modeSelectionObjet = true) { selectedPokemon ->
                    applyReward(reward, selectedPokemon)
                }
            } else {
                // Pas besoin de choisir un Poké pour l'or ou le soin d'équipe
                dialog.dismiss()
                applyReward(reward, activePokemon)
            }
        }
    }

    private fun applyReward(reward: Reward, targetPokemon: Pokemon) {
        val messages = mutableListOf<String>()
        val messageReward = reward.action(targetPokemon)
        messages.add(messageReward)

        //on vérif si évo
        if (reward.isTargeted && targetPokemon.species.evoLevel != null && targetPokemon.species.evoLevel!! <= targetPokemon.level) {
            messages.add("Hein ? ${targetPokemon.species.nom} évolue !")
            val oldName = targetPokemon.species.nom
            val oldLevel = targetPokemon.level
            targetPokemon.species = DataManager.model.creerPokemon(targetPokemon.species.evo).species
            targetPokemon.level = 1
            for (i in 1 until oldLevel) targetPokemon.monterLevel()
            targetPokemon.currentHp = targetPokemon.getMaxHp()
            messages.add("$oldName a évolué en ${targetPokemon.species.nom} !")
        }

        onRewardSelected(messages)
    }

    private fun pickRandomRewards(count: Int): List<Reward> {
        val picked = mutableListOf<Reward>()
        val pool = availableRewards.toMutableList()

        repeat(count) {
            if (pool.isNotEmpty()) {
                val totalWeight = pool.sumOf { it.weight }
                var randomValue = Random.nextInt(totalWeight)
                var selected: Reward? = null
                for (reward in pool) {
                    randomValue -= reward.weight
                    if (randomValue < 0) {
                        selected = reward
                        break
                    }
                }
                if (selected != null) {
                    picked.add(selected)
                    pool.remove(selected)
                }
            }
        }
        return picked
    }
}