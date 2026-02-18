package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import kotlin.random.Random

class RewardBattleVague(
    private val context: Context,
    private val activePokemon: Pokemon,
    private val onRewardSelected: () -> Unit
) {
    //UI
    val dialog = Dialog(context)

    //raretés avec leurs couleurs
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
        val action: (Pokemon) -> Unit
    )

    //liste des objets
    private val availableRewards = listOf(
        //COMMUNS
        Reward("lvl_1", "Bonbon", "+1 Niveau", R.drawable.bonbon, Rarity.COMMON, 50) { poke ->
            poke.monterLevel()
            Toast.makeText(context, "${poke.species.nom} gagne 1 niveau !", Toast.LENGTH_SHORT).show()
        },
        Reward("atk_1", "Attack +", "+5 Attaque (Perm.)", R.drawable.attaque_plus, Rarity.COMMON, 40) { poke ->
            poke.ajouterObjet("atk_plus")
            Toast.makeText(context, "Attaque augmentée de 5 !", Toast.LENGTH_SHORT).show()
        },
        Reward("pv_1", "PV +", "+10 PV Max (Perm.)", R.drawable.pv_plus, Rarity.COMMON, 40) { poke ->
            poke.ajouterObjet("pv_plus")
            poke.heal(10)
            Toast.makeText(context, "PV Max augmentés de 10 !", Toast.LENGTH_SHORT).show()
        },
        Reward("def_1", "Defense +", "+5 Défense (Perm.)", R.drawable.def_plus, Rarity.COMMON, 40) { poke ->
            poke.ajouterObjet("def_plus")
            Toast.makeText(context, "Défense augmentée de 5 !", Toast.LENGTH_SHORT).show()
        },
        Reward("vit_1", "Vitesse +", "+5 Vitesse (Perm.)", R.drawable.vit_plus, Rarity.COMMON, 40) { poke ->
            poke.ajouterObjet("vit_plus")
            Toast.makeText(context, "Vitesse augmentée de 5 !", Toast.LENGTH_SHORT).show()
        },
        Reward("heal_50", "Soin", "Soin 50%", R.drawable.potion, Rarity.COMMON, 60) { poke ->
            val amount = (poke.getMaxHp() * 0.5).toInt()
            poke.heal(amount)
            Toast.makeText(context, "${poke.species.nom} soigné de 50% !", Toast.LENGTH_SHORT).show()
        },

        //ÉPIQUES
        Reward("lvl_3", "Super Bonbon", "+3 Niveaux", R.drawable.super_bonbon, Rarity.EPIC, 15) { poke ->
            repeat(3) { poke.monterLevel() }
            Toast.makeText(context, "${poke.species.nom} gagne 3 niveaux !", Toast.LENGTH_SHORT).show()
        },
        Reward("atk_2", "Attack ++", "+10 Attaque (Perm.)", R.drawable.attaque_plus, Rarity.EPIC, 10) { poke ->
            poke.ajouterObjet("atk_plus_plus")
            Toast.makeText(context, "Attaque augmentée de 10 !", Toast.LENGTH_SHORT).show()
        },
        Reward("pv_2", "PV ++", "+20 PV Max (Perm.)", R.drawable.pv_plus, Rarity.EPIC, 10) { poke ->
            poke.ajouterObjet("pv_plus_plus")
            poke.heal(20)
            Toast.makeText(context, "PV Max augmentés de 20 !", Toast.LENGTH_SHORT).show()
        },
        Reward("def_2", "Defense ++", "+10 Défense (Perm.)", R.drawable.def_plus, Rarity.EPIC, 10) { poke ->
            poke.ajouterObjet("def_plus_plus")
            Toast.makeText(context, "Défense augmentée de 10 !", Toast.LENGTH_SHORT).show()
        },
        Reward("vit_2", "Vitesse ++", "+10 Vitesse (Perm.)", R.drawable.vit_plus, Rarity.EPIC, 10) { poke ->
            poke.ajouterObjet("vit_plus_plus")
            Toast.makeText(context, "Vitesse augmentée de 10 !", Toast.LENGTH_SHORT).show()
        },
        Reward("heal_100", "Super Soin", "Soin 100%", R.drawable.super_potion, Rarity.EPIC, 25) { poke ->
            poke.heal(poke.getMaxHp())
            Toast.makeText(context, "${poke.species.nom} entièrement soigné !", Toast.LENGTH_SHORT).show()
        },

        //LÉGENDAIRES
        Reward("heal_team", "Hyper Soin", "Soin Équipe 100%", R.drawable.hyper_potion, Rarity.LEGENDARY, 5) { _ ->
            Player.getEquipe().forEach { it.heal(it.getMaxHp()) }
            Toast.makeText(context, "Toute l'équipe est soignée !", Toast.LENGTH_SHORT).show()
        },
        Reward("rappel_max", "Rappel Max", "Réanime toute l'équipe (50%)", R.drawable.rappel_max, Rarity.LEGENDARY, 5) { _ ->
            var count = 0
            Player.getEquipe().forEach {
                if (it.isKO) {
                    it.isKO = false
                    it.currentHp = it.getMaxHp() / 2
                    count++
                }
            }
            if(count > 0) Toast.makeText(context, "$count Pokémon réanimés !", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Aucun Pokémon K.O.", Toast.LENGTH_SHORT).show()
        },
        Reward("gold", "PokéGold", "Or aléatoire (10-50)", R.drawable.gold, Rarity.LEGENDARY, 35) { _ ->
            val amount = Random.nextInt(10, 51)
            if (context is PlayActivity) {
                context.ajouterOr(amount)
            }
            Player.addPieces(amount)
            Toast.makeText(context, "Gagné $amount PokéGold !", Toast.LENGTH_SHORT).show()
        },
        Reward("item_restes", "Restes", "Soigne à chaque tour", R.drawable.restes, Rarity.LEGENDARY, 5) { poke ->
            poke.ajouterObjet("item_restes")
            Toast.makeText(context, "${poke.species.nom} a équipé Restes !", Toast.LENGTH_SHORT).show()
        },
        Reward("item_bague_force", "Bague Force", "+25 Attaque (Perm.)", R.drawable.bague_force, Rarity.LEGENDARY, 5) { poke ->
            poke.ajouterObjet("item_bague_force")
            Toast.makeText(context, "Attaque augmentée drastiquement !", Toast.LENGTH_SHORT).show()
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
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.7f)

            //taille du popup
            val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
            val height = (context.resources.displayMetrics.heightPixels * 0.60).toInt()
            window.setLayout(width, height)
        }

        val selectedRewards = pickRandomRewards(3)

        setupRewardView(selectedRewards[0], R.id.containerObj1, R.id.imgObj1, R.id.nomObj1)
        setupRewardView(selectedRewards[1], R.id.containerObj2, R.id.imgObj2, R.id.nomObj2)
        setupRewardView(selectedRewards[2], R.id.containerObj3, R.id.imgObj3, R.id.nomObj3)

        dialog.show()
    }

    private fun setupRewardView(reward: Reward, containerId: Int, imgId: Int, txtId: Int) {
        val container = dialog.findViewById<ConstraintLayout>(containerId)
        val img = dialog.findViewById<ImageView>(imgId)
        val txt = dialog.findViewById<TextView>(txtId)

        txt.text = reward.name
        txt.setTextColor(reward.rarity.color)

        Glide.with(context).load(reward.imageRes).into(img)

        container.setOnClickListener {
            reward.action(activePokemon)
            dialog.dismiss()
            onRewardSelected()
        }
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