package com.example.idlemon

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import kotlin.random.Random

class PlayActivity : BaseActivity() {

    //ui global
    private lateinit var txtDialogueBattle: TextView
    private lateinit var return_btn: ImageView
    private lateinit var numVague: TextView

    //menu
    private lateinit var layoutMenuPrincipal: View
    private lateinit var layoutMenuEquipe: View //dialog équipe cachée par défaut

    //menue équipe
    private lateinit var teamListContainer: LinearLayout
    private lateinit var btnCloseTeam: ImageView

    //ennemi
    private lateinit var imgPokeEnemy: ImageView
    private lateinit var txtEnemyName: TextView
    private lateinit var txtEnemyLvl: TextView
    private lateinit var enemyHpBar: ProgressBar

    //player
    private lateinit var imgPokePlayer: ImageView
    private lateinit var txtPlayerName: TextView
    private lateinit var txtPlayerLvl: TextView
    private lateinit var playerHpBar: ProgressBar
    private lateinit var txtPlayerHpText: TextView

    //btn
    private lateinit var btnAttack: ConstraintLayout
    private lateinit var btnTeam: ConstraintLayout

    //logique jeu
    private lateinit var playerPokemon: Pokemon
    private lateinit var enemyPokemon: Pokemon

    private var isTurnInProgress = false
    private var nbPieceGagnee = 0
    private var currentTurn = 1
    private var vagueActuelle = 1
    val modelJson = DataManager.model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setupFullscreen()

        initViews()
        setupBattle()

        //attaquer btn
        btnAttack.setOnClickListener {
            if (!isTurnInProgress) {
                val dialog = BattleAttackDialog(this, playerPokemon) { attack ->
                    //quand on clique sur uen attaque
                    executerTourDeJeu(attack)
                }
                dialog.show()
            }
        }

        //Team btn
        btnTeam.setOnClickListener {
            if (!isTurnInProgress) {
                afficherMenuEquipe(false)
            }
        }

        //fermer equipe btn
        btnCloseTeam.setOnClickListener {
            layoutMenuEquipe.visibility = View.GONE
        }

        //quitter btn
        return_btn.setOnClickListener {
            resetPartieDonnees()
            finish()
        }
    }

    private fun initViews() {
        txtDialogueBattle = findViewById(R.id.txt_dialogue_battle)
        return_btn = findViewById(R.id.return_btn)
        numVague = findViewById(R.id.numVague)

        layoutMenuPrincipal = findViewById(R.id.layout_menu_principal)
        layoutMenuEquipe = findViewById(R.id.layout_menu_equipe)

        teamListContainer = findViewById(R.id.team_list_container)
        btnCloseTeam = findViewById(R.id.btn_close_team)

        imgPokeEnemy = findViewById(R.id.imgPokeEnemy)
        txtEnemyName = findViewById(R.id.txt_enemy_name)
        txtEnemyLvl = findViewById(R.id.txt_enemy_lvl)
        enemyHpBar = findViewById(R.id.enemy_hp_bar)

        imgPokePlayer = findViewById(R.id.imgPokePlayer)
        txtPlayerName = findViewById(R.id.txt_player_name)
        txtPlayerLvl = findViewById(R.id.txt_player_lvl)
        playerHpBar = findViewById(R.id.player_hp_bar)
        txtPlayerHpText = findViewById(R.id.txt_player_hp_text)

        btnAttack = findViewById(R.id.btn_attack_container)
        btnTeam = findViewById(R.id.btn_team_container)
    }

    //équipe avec etaitKo qui permet de savoir si on switch ou on est forcé car un pokémon vient d'être KO
    private fun afficherMenuEquipe(etaitKo: Boolean) {
        layoutMenuEquipe.visibility = View.VISIBLE
        teamListContainer.removeAllViews()

        for (pokemon in Player.getEquipe()) {
            val itemView = layoutInflater.inflate(R.layout.item_team_battle, teamListContainer, false)
            //init vue
            val name = itemView.findViewById<TextView>(R.id.item_poke_name)
            val lvl = itemView.findViewById<TextView>(R.id.item_poke_lvl)
            val hpBar = itemView.findViewById<ProgressBar>(R.id.item_poke_hp_bar)
            val hpText = itemView.findViewById<TextView>(R.id.item_poke_hp_text)
            val icon = itemView.findViewById<ImageView>(R.id.item_poke_icon)

            name.text = pokemon.species.nom
            lvl.text = "Lv. ${pokemon.level}"
            val maxHp = pokemon.getMaxHp()
            hpBar.max = maxHp
            hpBar.progress = pokemon.currentHp
            hpText.text = "${pokemon.currentHp}/$maxHp"
            updateHpColor(hpBar, pokemon.currentHp, maxHp)
            //gif poké
            Glide.with(this).load(DataManager.model.getFrontSprite(pokemon.species.num)).into(icon)
            //actif ou ko
            if (pokemon.isKO) itemView.alpha = 0.5f
            if (pokemon == playerPokemon) name.text = "${pokemon.species.nom} (Actif)"

            //switch gestion
            itemView.setOnClickListener {
                if (pokemon == playerPokemon) {
                    Toast.makeText(this, "Déjà au combat !", Toast.LENGTH_SHORT).show()
                } else if (pokemon.isKO) {
                    Toast.makeText(this, "Ce Pokémon est K.O.", Toast.LENGTH_SHORT).show()
                } else {
                    //etaitKO est à false car on vient de changer de pokémon
                    changerPokemon(pokemon, etaitKo)
                    layoutMenuEquipe.visibility = View.GONE
                }
            }
            teamListContainer.addView(itemView)
        }
    }

    //Combat

    private fun setupBattle() {
        //on quitte si l'équipe est vide à enlever + tard
        if (Player.getEquipe().isEmpty()) {
            finish()
            return
        }

        if (!::playerPokemon.isInitialized) {
            playerPokemon = Player.getPremierPokemon()
        }
        if (playerPokemon.isKO) {
            playerPokemon = Player.getPremierPokemon()
        }

        //genere l'ennemi random et actualise l'UI
        genererEnnemi()
        updateUI(animate = false)
        //annime arrivée
        animateEntry(imgPokePlayer, isPlayer = true)
        animateEntry(imgPokeEnemy, isPlayer = false)
        //tour et init texte tour
        currentTurn = 1
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        txtDialogueBattle.text = "Un ${enemyPokemon.species.nom} sauvage apparaît !"
    }

    private fun genererEnnemi() {
        enemyPokemon = modelJson.getRandomPokemon()
        //level proche du joueur à changer
        val baseLvl = playerPokemon.level
        val randomLvl = (baseLvl - 2..baseLvl + 2).random().coerceAtLeast(1)
        enemyPokemon.level = 1
        for (i in 1 until randomLvl) enemyPokemon.monterLevel()
        enemyPokemon.currentHp = enemyPokemon.getMaxHp()

        //on donne 4 attaques random
        val attaquesDispo = modelJson.getAttackDispo(enemyPokemon)
        enemyPokemon.attacks.clear()
        val selectedAttacks = attaquesDispo.shuffled().take(4)
        for (atk in selectedAttacks) enemyPokemon.addAttack(atk)
        //init pour animation
        imgPokeEnemy.alpha = 1f
        imgPokeEnemy.scaleX = 1f
        imgPokeEnemy.scaleY = 1f
    }

    private fun changerPokemon(newPokemon: Pokemon, etaitKo: Boolean) {
        isTurnInProgress = true

        animateSwitchOut(imgPokePlayer) {
            playerPokemon = newPokemon
            updateUI(animate = false)
            txtDialogueBattle.text = "Go ! ${playerPokemon.species.nom} !"
            animateEntry(imgPokePlayer, true)

            Handler(Looper.getMainLooper()).postDelayed({
                if(etaitKo) {
                    isTurnInProgress = false
                    txtDialogueBattle.text = "Que doit faire ${playerPokemon.species.nom} ?"
                } else {
                    tourEnnemiSeul()
                }
            }, 1000)
        }
    }

    private fun executerTourDeJeu(playerAttack: Attack) {
        isTurnInProgress = true
        currentTurn++
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        val enemyAttack = enemyPokemon.attacks.random()
        val pSpeed = playerPokemon.currentVit
        val eSpeed = enemyPokemon.currentVit
        //si vitesse égale, on choisit random
        val playerFirst = if (pSpeed == eSpeed) Random.nextBoolean() else pSpeed > eSpeed

        //faire attaque
        if (playerFirst) {
            sequenceAttaque(playerPokemon, imgPokePlayer, enemyPokemon, imgPokeEnemy, playerAttack, enemyAttack, true)
        } else {
            sequenceAttaque(enemyPokemon, imgPokeEnemy, playerPokemon, imgPokePlayer, enemyAttack, playerAttack, false)
        }
    }

    //fonction qui gère l'attaque
    private fun sequenceAttaque(first: Pokemon, viewFirst: ImageView, second: Pokemon, viewSecond: ImageView, move1: Attack, move2: Attack, isPlayerFirst: Boolean) {
        txtDialogueBattle.text = "${first.species.nom} utilise ${move1.name} !"
        animateAttackMove(viewFirst, isPlayerFirst) {
            applicationDegats(first, second, move1)
            animateHit(viewSecond)
            if (second.isKO) {
                animateKO(viewSecond) {
                    finDuCombat(second)
                }
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    txtDialogueBattle.text = "${second.species.nom} utilise ${move2.name} !"
                    animateAttackMove(viewSecond, !isPlayerFirst) {
                        applicationDegats(second, first, move2)
                        animateHit(viewFirst)
                        if (first.isKO) {
                            animateKO(viewFirst) { finDuCombat(first) }
                        } else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                isTurnInProgress = false
                                txtDialogueBattle.text = "Que doit faire ${playerPokemon.species.nom} ?"
                            }, 1000)
                        }
                    }
                }, 1000)
            }
        }
    }

    //le cas ou le Player fait un Changement de poké
    private fun tourEnnemiSeul() {
        if(playerPokemon.isKO) {
            isTurnInProgress = false
            return
        }

        val enemyAttack = enemyPokemon.attacks.random()
        txtDialogueBattle.text = "${enemyPokemon.species.nom} utilise ${enemyAttack.name} !"

        animateAttackMove(imgPokeEnemy, false) {
            applicationDegats(enemyPokemon, playerPokemon, enemyAttack)
            animateHit(imgPokePlayer)

            if (playerPokemon.isKO) {
                animateKO(imgPokePlayer) { finDuCombat(playerPokemon) }
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    isTurnInProgress = false
                    txtDialogueBattle.text = "Que doit faire ${playerPokemon.species.nom} ?"
                }, 1000)
            }
        }
    }

    private fun applicationDegats(attaquant: Pokemon, defenseur: Pokemon, attaque: Attack) {
        //gestion PP
        val atkIndex = attaquant.attacks.indexOf(attaque)
        if (atkIndex != -1) {
            val currentPP = attaquant.currentPP[atkIndex] ?: attaque.pp
            if (currentPP > 0) attaquant.currentPP[atkIndex] = currentPP - 1
        }

        //gestion Heal
        if (attaque.heal > 0) {
            val healAmount = (attaquant.getMaxHp() * attaque.heal) / 100
            attaquant.heal(healAmount)
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isDestroyed) Toast.makeText(this, "${attaquant.species.nom} récupère de la vie !", Toast.LENGTH_SHORT).show()
            }, 500)
            updateUI(animate = true)
            appliquerEffetsStats(attaquant, defenseur, attaque)
            return
        }

        //init puissance et def
        val puissance = if (attaque.basePower > 0) attaque.basePower else 0
        val levelFactor = (2 * attaquant.level / 5) + 2
        val statRatio = attaquant.currentAtk.toDouble() / defenseur.currentDef.toDouble()
        var degats = (((levelFactor * puissance * statRatio) / 50) + 2).toDouble()

        //on met à 1 dmg minimum si c'est une attaque tres tres faible
        if (degats < 1 && puissance > 0) degats = 1.0
        if (puissance == 0) degats = 0.0

        //gestion critique
        val isCrit = Random.nextDouble() < attaque.critRatio
        if (isCrit && degats > 0) degats *= 1.5

        val typeAtkEnum = try {
            PokemonType.valueOf(attaque.type.uppercase())
        } catch (e: Exception) {
            PokemonType.NORMAL
        }
        val multiplicateur = PokemonType.calculerEfficaciteContre(typeAtkEnum, defenseur)
        var degatsFinal = (degats * multiplicateur).toInt()

        //gestion dialog efficacité
        if (degatsFinal > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isDestroyed) {
                    var msg = ""
                    if (isCrit) msg = "Coup critique !\n"

                    if (multiplicateur > 2.0) {
                        msg += "C'est extrêmement efficace !"
                        txtDialogueBattle.text = "C'est extrêmement efficace !"
                    }else if (multiplicateur.toInt() == 2 ){
                        msg += "C'est super efficace !"
                        txtDialogueBattle.text = "C'est super efficace !"
                    }else if (multiplicateur == 0.5){
                        msg += "Ce n'est pas très efficace !"
                        txtDialogueBattle.text = "Ce n'est pas très efficace !"
                    }else if(multiplicateur == 0.25){
                        msg += "C'est extrêmement inefficace !"
                        txtDialogueBattle.text = "C'est extrêmement inefficace !"
                    }
                    if(msg.isNotEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }, 500)
        }

        defenseur.prendreDmg(degatsFinal)

        //gestion drain
        if (attaque.drain && degatsFinal > 0) {
            val drainAmount = degatsFinal / 2
            if (drainAmount > 0) {
                attaquant.heal(drainAmount)
                Handler(Looper.getMainLooper()).postDelayed({
                    if(!isDestroyed) Toast.makeText(this, "Vie drainée !", Toast.LENGTH_SHORT).show()
                }, 600)
            }
        }

        //gestion bonus/malus
        appliquerEffetsStats(attaquant, defenseur, attaque)

        updateUI(animate = true)
    }

    private fun appliquerEffetsStats(attaquant: Pokemon, defenseur: Pokemon, attaque: Attack) {
        attaque.bonus?.forEach { map ->
            map.forEach { (stat, valeur) -> appliquerStatChange(attaquant, stat, valeur) }
        }
        attaque.malus?.forEach { map ->
            map.forEach { (stat, valeur) -> appliquerStatChange(defenseur, stat, valeur) }
        }
    }

    private fun appliquerStatChange(cible: Pokemon, stat: String, niveau: Int) {
        val facteur = if (niveau > 0) 1.5 else 0.66
        var msg = ""
        when (stat.lowercase()) {
            "atk" -> {
                cible.currentAtk = (cible.currentAtk * facteur).toInt()
                msg = if (niveau > 0) "${cible.species.nom} monte son Attaque !" else "${cible.species.nom} voit son Attaque baisser !"
            }
            "def" -> {
                cible.currentDef = (cible.currentDef * facteur).toInt()
                msg = if (niveau > 0) "${cible.species.nom} monte sa Défense !" else "${cible.species.nom} voit sa Défense baisser !"
            }
            "vit" -> {
                cible.currentVit = (cible.currentVit * facteur).toInt()
                msg = if (niveau > 0) "${cible.species.nom} monte sa Vitesse !" else "${cible.species.nom} voit sa Vitesse baisser !"
            }
        }
        if (msg.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isDestroyed) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }, 800)
        }
    }

    private fun finDuCombat(loser: Pokemon) {
        if (loser == enemyPokemon) {
            txtDialogueBattle.text = "Ennemi K.O. ! Victoire !"
            nbPieceGagnee += 100

            //20 + moyenne des stats du poké + (level/7)
            val xpGain = 20 + (enemyPokemon.currentAtk + enemyPokemon.currentDef + enemyPokemon.currentVit + enemyPokemon.getMaxHp())/5 + (enemyPokemon.level/7)
            val aLevelUp = playerPokemon.gagnerExperience(xpGain)
            var msg = "Vous gagnez $xpGain XP."
            if (aLevelUp){
                msg += "\n${playerPokemon.species.nom} monte au niveau ${playerPokemon.level} !"
                //MusicManager.playNotifLvlUp()
            }
            //cas d'évolution peut etre à revoir
            if(playerPokemon.species.evo != null && playerPokemon.species.evoLevel!! <= playerPokemon.level){
                msg += "\n${playerPokemon.species.nom} à évolué en ${playerPokemon.species.evo} !"
                var oldLevel = playerPokemon.level
                playerPokemon.species = modelJson.creerPokemon(playerPokemon.species.evo).species
                playerPokemon.level = 1
                for (i in 1 until oldLevel) playerPokemon.monterLevel() //pour augementer les stats
                playerPokemon.currentHp = playerPokemon.getMaxHp()
                updateUI(animate = false)

            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
                if (!isDestroyed && !isFinishing) {
                    vagueActuelle++
                    currentTurn = 1
                    numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
                    setupBattle()
                    isTurnInProgress = false
                }
            }, 4000)
        } else {
            txtDialogueBattle.text = "${playerPokemon.species.nom} est K.O..."
            isTurnInProgress = false
            if (Player.getEquipe().all { it.isKO }) {
                Toast.makeText(this, "GAME OVER", Toast.LENGTH_LONG).show()
                resetPartieComplet()
                finish()
            } else {
                afficherMenuEquipe(true)
            }
        }
    }

    fun resetPartieComplet(){
        resetPartieDonnees()
        vagueActuelle = 1
        currentTurn = 1
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        setupBattle()
        isTurnInProgress = false
        txtDialogueBattle.text = "Un ${enemyPokemon.species.nom} sauvage apparaît !"
        updateUI(animate = false)
        //plus tard, afficher dialogue de récompense de fin de partie
    }

    private fun resetPartieDonnees() {
        nbPieceGagnee = 0
        vagueActuelle = 1
        currentTurn = 1
        for (pokemon in Player.getEquipe()) {
            pokemon.isKO = false
            pokemon.currentHp = pokemon.getMaxHp()
            pokemon.level = 1
            pokemon.exp = 0
            //reset stats combat (manuellement si pas de fonction dédiée)
            pokemon.currentAtk = ((2 * pokemon.species.baseStats.atk * pokemon.level) / 100) + 5
            pokemon.currentDef = ((2 * pokemon.species.baseStats.def * pokemon.level) / 100) + 5
            pokemon.currentVit = ((2 * pokemon.species.baseStats.vit * pokemon.level) / 100) + 5
        }
    }

    //animation
    private fun animateEntry(view: View, isPlayer: Boolean) {
        view.translationX = if (isPlayer) -500f else 500f
        view.alpha = 0f
        view.scaleX = 0.5f; view.scaleY = 0.5f
        view.animate().translationX(0f).alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setInterpolator(DecelerateInterpolator()).start()
    }
    private fun animateSwitchOut(view: View, onEnd: () -> Unit) {
        view.animate().translationX(-500f).alpha(0f).setDuration(500).withEndAction(onEnd).start()
    }
    private fun animateAttackMove(view: View, isPlayer: Boolean, onImpact: () -> Unit) {
        view.animate().translationX(if (isPlayer) 300f else -300f).translationY(if (isPlayer) -100f else 100f).setDuration(250).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction {
            onImpact()
            view.animate().translationX(0f).translationY(0f).setDuration(300).start()
        }.start()
    }
    private fun animateHit(view: View) {
        view.animate().translationX(20f).setDuration(50).withEndAction { view.animate().translationX(-20f).setDuration(50).withEndAction { view.animate().translationX(0f).setDuration(50).start() }.start() }.start()
    }
    private fun animateKO(view: View, onEnd: () -> Unit) {
        view.animate().scaleX(0f).scaleY(0f).alpha(0f).rotation(360f).setDuration(800).withEndAction(onEnd).start()
    }

    //UI
    private fun updateUI(animate: Boolean) {
        if(isDestroyed || isFinishing) return

        //player
        txtPlayerName.text = playerPokemon.species.nom
        txtPlayerLvl.text = "Lv${playerPokemon.level}"
        val maxHpP = playerPokemon.getMaxHp()

        if (animate) {
            animateHpChange(playerHpBar, txtPlayerHpText, playerPokemon.currentHp, maxHpP)
        } else {
            playerHpBar.max = maxHpP
            playerHpBar.progress = playerPokemon.currentHp
            txtPlayerHpText.text = "${playerPokemon.currentHp} / $maxHpP"
            updateHpColor(playerHpBar, playerPokemon.currentHp, maxHpP)
        }
        Glide.with(this).load(DataManager.model.getBackSprite(playerPokemon.species.num)).into(imgPokePlayer)

        //enemie
        txtEnemyName.text = enemyPokemon.species.nom
        txtEnemyLvl.text = "Lv${enemyPokemon.level}"
        val maxHpE = enemyPokemon.getMaxHp()

        if (animate) {
            animateHpChange(enemyHpBar, null, enemyPokemon.currentHp, maxHpE)
        } else {
            enemyHpBar.max = maxHpE
            enemyHpBar.progress = enemyPokemon.currentHp
            updateHpColor(enemyHpBar, enemyPokemon.currentHp, maxHpE)
        }
        Glide.with(this).load(DataManager.model.getFrontSprite(enemyPokemon.species.num)).into(imgPokeEnemy)
    }

    private fun animateHpChange(bar: ProgressBar, textViewHp: TextView?, targetHp: Int, maxHp: Int) {
        if(isDestroyed || isFinishing) return

        bar.max = maxHp
        val currentProgress = bar.progress
        if (currentProgress == targetHp) {
            updateHpColor(bar, targetHp, maxHp)
            if (textViewHp != null) textViewHp.text = "$targetHp / $maxHp"
            return
        }
        val animation = ObjectAnimator.ofInt(bar, "progress", currentProgress, targetHp)
        animation.duration = 600
        animation.interpolator = FastOutSlowInInterpolator()

        animation.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            updateHpColor(bar, animatedValue, maxHp)
            if (textViewHp != null) {
                textViewHp.text = "$animatedValue / $maxHp"
            }
        }
        animation.start()
    }

    private fun updateHpColor(bar: ProgressBar, current: Int, max: Int) {
        val percent = (current.toFloat() / max.toFloat()) * 100
        val drawableRes = when {
            percent > 50 -> R.drawable.hp_bar_green
            percent > 20 -> R.drawable.hp_bar_yellow
            else -> R.drawable.hp_bar_red
        }
        bar.progressDrawable = getDrawable(drawableRes)
    }

    private fun setupFullscreen() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}