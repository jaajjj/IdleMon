package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide

class ChangeAttackDialog(
    private val context: Context,
    private val pokemon: Pokemon
) {
    val dialog = Dialog(context)
    private var selectedIndex: Int = -1
    private lateinit var imgPokemonChange: ImageView

    fun show() {
        dialog.setContentView(R.layout.dialog_change_attack)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.7f)
        }
        val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
        val nomPoke = dialog.findViewById<TextView>(R.id.nomPoke)
        imgPokemonChange = dialog.findViewById<ImageView>(R.id.imgPoke)
        nomPoke.text = pokemon.species.nom
        Glide.with(context)
            .asGif()
            .load(DataManager.model.getFrontSprite(pokemon.species.num))
            .fitCenter()
            .into(imgPokemonChange)

        refreshCurrentAttacks()
        refreshAvailableAttacks()
        closeBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.85).toInt()
        dialog.window?.setLayout(width, height)
    }

    private fun refreshCurrentAttacks() {
        val container = dialog.findViewById<LinearLayout>(R.id.attackContainer)
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (i in 0 until 4) {
            val isSlotOccupee = i < pokemon.attacks.size
            val layoutId = if (isSlotOccupee) R.layout.item_attack_view else R.layout.item_attack_vide_view
            val itemView = inflater.inflate(layoutId, container, false)
            itemView.alpha = if (selectedIndex == i) 0.3f else 1.0f
            if (isSlotOccupee) {
                fillAttackInfo(itemView, pokemon.attacks[i])
            }
            itemView.setOnClickListener {
                selectedIndex = if (selectedIndex == i) -1 else i
                refreshCurrentAttacks()
                refreshAvailableAttacks()
            }
            itemView.setOnLongClickListener {
                if (i < pokemon.attacks.size) {
                    if (pokemon.attacks.size > 1) { //on supp pas si 0 attaques
                        pokemon.attacks.removeAt(i)
                        selectedIndex = -1
                        refreshCurrentAttacks()
                        refreshAvailableAttacks()
                    }
                }
                true //termine le listenenr
            }
            container.addView(itemView)
        }
    }

    private fun refreshAvailableAttacks() {
        val list = dialog.findViewById<LinearLayout>(R.id.boxLinearLayout)
        val scrollBox = dialog.findViewById<View>(R.id.attackDispoScrollView)
        list.removeAllViews()
        scrollBox.alpha = if (selectedIndex == -1) 0.3f else 1.0f
        val inflater = LayoutInflater.from(context)
        val attacksDispo = DataManager.model.getAttackDispo(pokemon)
        for (atk in attacksDispo) {
            val itemView = inflater.inflate(R.layout.item_attack_view, list, false)
            fillAttackInfo(itemView, atk)
            itemView.setOnClickListener {
                if (selectedIndex != -1) {
                    pokemon.replaceAttack(selectedIndex, atk)
                    selectedIndex = -1
                    refreshCurrentAttacks()
                    refreshAvailableAttacks()
                }
            }
            list.addView(itemView)
        }
    }

    private fun fillAttackInfo(view: View, atk: Attack) {
        view.findViewById<TextView>(R.id.nomAttack).text = atk.name
        view.findViewById<TextView>(R.id.descAttack).text = atk.description
        view.findViewById<TextView>(R.id.dmgTextView).text = atk.basePower.toString()
        view.findViewById<TextView>(R.id.accTextView).text = (atk.accuracy * 100).toInt().toString()
        view.findViewById<TextView>(R.id.ppMaxTextView).text = atk.pp.toString()

        val ids = listOf(R.id.nomAttack, R.id.descAttack, R.id.dmgTextView, R.id.accTextView, R.id.ppMaxTextView)
        //correction où le texte noir ne voulait pas apparaitre
        ids.forEach { view.findViewById<TextView>(it).setTextColor(android.graphics.Color.BLACK) }

        view.findViewById<ImageView>(R.id.CtTypeImg).setImageResource(getIconType(atk.type))
    }

    private fun getIconType(typeName: String): Int {
        return when (typeName.replaceFirstChar { it.uppercase() }) {
            "Acier" -> R.drawable.ct_acier
            "Combat" -> R.drawable.ct_combat
            "Dragon" -> R.drawable.ct_dragon
            "Eau" -> R.drawable.ct_eau
            "Feu" -> R.drawable.ct_feu
            "Fee" -> R.drawable.ct_fee
            "Glace" -> R.drawable.ct_glace
            "Insecte" -> R.drawable.ct_insecte
            "Normal" -> R.drawable.ct_normal
            "Plante" -> R.drawable.ct_plante
            "Poison" -> R.drawable.ct_poison
            "Psy" -> R.drawable.ct_psy
            "Roche" -> R.drawable.ct_roche
            "Sol" -> R.drawable.ct_sol
            "Spectre" -> R.drawable.ct_spectre
            "Tenebre" -> R.drawable.ct_tenebre
            "Vol" -> R.drawable.ct_vol
            "Electrik" -> R.drawable.ct_electrik
            else -> R.drawable.ct_normal
        }
    }
}