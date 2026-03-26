package com.example.idlemon

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.HtmlCompat

object RulesManager {

    // structure de données pour stocker chaque page (IMG + Texte avec mots clés)
    private data class RulePage(val imageRes: Int, val htmlText: String)

    private const val TEXTE1 = "Bienvenue dans <font color='#ffc800'><b><big>IdleMon</big></b></font> !<br><br>" +
            "Voici les règles du jeu pour bien débuter votre aventure de <font color='#ffa100'><b>Dresseur Pokémon</b></font>."

    private const val TEXTE2 = "Votre objectif est de capturer tous les " +
            "<font color='#ffc800'><b><big>Pokémon</big></b></font> de ce monde.<br><br>" +
            "Utilisez vos <font color='#ffa100'><b><big>PokéGold</big></b></font> <img src='gold'> pour invoquer " +
            "de nouveaux Pokémon dans la page <font color='#ff8200'><b><big>Gacha</big></b></font> <img src='loupe'>."

    private const val TEXTE3 = "Une fois que vous aurez obtenu de nouveaux Pokémon, " +
            "vous pourrez les ajouter à votre <font color='#ffc800'><b><big>équipe</big></b></font> <img src='team_footer'>.<br><br>" +
            "De plus, amusez-vous à personnaliser leurs attaques pour les rendre encore plus <font color='#ffa100'><b>redoutables.</b></font>"

    private const val TEXTE4 = "Une fois votre équipe de rêve construite, lancez-vous dans un mode de " +
            "<font color='#ffc800'><b><big>combat infini</big></b></font> <br><br> et affrontez des Pokémon sauvages plus puissants les uns que les autres."

    private const val TEXTE5 = "Lorsqu'un Pokémon adverse est battu, choisissez un <font color='#ffc800'><b><big>Objet</big></b></font> " +
            "pour <font color='#ffa100'><b><big>booster</big></b></font> votre équipe."

    private const val TEXTE6 = "Toutes les 10 vagues, un <font color='#ffc800'><b><big>Boss</big></b></font> apparaît. Remportez " +
            "ce combat pour obtenir de meilleures récompenses !"

    private const val TEXTE7 = "<font color='#ffc800'><b><big>Attention</big></b></font> ! Toute partie lancée " +
            "ne peut être sauvegardée en cours de route. Quittez donc avec précaution !"

    private const val TEXTE8 = "N'oubliez pas de vous <b><big>créer un compte</big></b> afin de sécuriser votre sauvegarde. " +
            "Pour cela, rendez-vous sur la page <font color='#ffc800'><b><big>Paramètres</big></b></font> <img src='settings'>."

    fun showRulesDialog(activity: Activity, onClose: () -> Unit) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_rules)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val metrics = activity.resources.displayMetrics
        val width = (metrics.widthPixels * 0.90).toInt()
        val height = (metrics.heightPixels * 0.70).toInt()

        dialog.window?.setLayout(width, height)

        val closeRulesBtn = dialog.findViewById<ImageView>(R.id.closeRulesBtn)
        val imgRule = dialog.findViewById<ImageView>(R.id.imgRule)
        val tvRuleText = dialog.findViewById<TextView>(R.id.tvRuleText)
        val btnPrevRule = dialog.findViewById<ImageView>(R.id.btnPrevRule)
        val btnNextRule = dialog.findViewById<ImageView>(R.id.btnNextRule)
        val tvPageIndicator = dialog.findViewById<TextView>(R.id.tvPageIndicator)

        //Regles sous forme de images + text avec couleurs si nécessaire
        val pages = listOf(
            RulePage(R.drawable.icon_rounded, TEXTE1),
            RulePage(R.drawable.rules_eggs, TEXTE2),
            RulePage(R.drawable.gold, TEXTE3),
            RulePage(R.drawable.rules_battle1, TEXTE4),
            RulePage(R.drawable.rules_objet, TEXTE5),
            RulePage(R.drawable.rules_objet, TEXTE6),
            RulePage(R.drawable.rules_login, TEXTE7)
        )

        var currentPage = 0

        val imageGetter = Html.ImageGetter { source ->
            //récup id de l'image
            val id = activity.resources.getIdentifier(source, "drawable", activity.packageName)

            if (id != 0) {
                //charge et cherche le drawable
                val drawable = ContextCompat.getDrawable(activity, id)

                if (drawable != null) {
                    //ajustement de la taille en hauteur de l'img
                    val heightConstraint = (tvRuleText.lineHeight * 1.3).toInt()

                    //ajustement de la taille en largeur de l'img
                    val aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                    val widthConstraint = (heightConstraint * aspectRatio).toInt()

                    //limites du drawable
                    drawable.setBounds(0, 0, widthConstraint, heightConstraint)
                    return@ImageGetter drawable
                }
            }
            // Retourner null si l'image n'est pas trouvée
            null
        }

        //update la page actuelle
        fun updatePage() {
            val page = pages[currentPage]
            imgRule.setImageResource(page.imageRes)
            tvRuleText.text = Html.fromHtml(page.htmlText, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)

            tvPageIndicator.text = "${currentPage + 1} / ${pages.size}"

            //afficher/Cacher les flèches
            btnPrevRule.visibility = if (currentPage == 0) View.INVISIBLE else View.VISIBLE
            btnNextRule.visibility = if (currentPage == pages.size - 1) View.INVISIBLE else View.VISIBLE

            // Remonter le scroll en haut à chaque changement de page
            dialog.findViewById<View>(R.id.scrollViewText).scrollTo(0, 0)
        }

        btnPrevRule.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        btnNextRule.setOnClickListener {
            if (currentPage < pages.size - 1) {
                currentPage++
                updatePage()
            }
        }

        closeRulesBtn.setOnClickListener {
            dialog.dismiss()
            onClose() //déclancher le onClose prévu avant (on dépends pas de SettingsManager)
        }

        //au démarage on prends la 1ere page
        updatePage()
        dialog.show()
    }
}