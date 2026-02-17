package com.example.idlemon

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
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

    // --- UI GLOBALE ---
    private lateinit var txtDialogueBattle: TextView
    private lateinit var return_btn: ImageView
    private lateinit var numVague: TextView

    // --- MENUS ---
    private lateinit var layoutMenuPrincipal: View
    private lateinit var layoutMenuEquipe: View // L'overlay équipe

    // --- MENU ÉQUIPE (Contenu) ---
    private lateinit var teamListContainer: LinearLayout
    private lateinit var btnCloseTeam: ImageView

    // --- ENNEMI ---
    private lateinit var imgPokeEnemy: ImageView
    private lateinit var txtEnemyName: TextView
    private lateinit var txtEnemyLvl: TextView
    private lateinit var enemyHpBar: ProgressBar

    // --- JOUEUR ---
    private lateinit var imgPokePlayer: ImageView
    private lateinit var txtPlayerName: TextView
    private lateinit var txtPlayerLvl: TextView
    private lateinit var playerHpBar: ProgressBar
    private lateinit var txtPlayerHpText: TextView

    // --- BOUTONS PRINCIPAUX ---
    private lateinit var btnAttack: ConstraintLayout
    private lateinit var btnTeam: ConstraintLayout

    // --- LOGIQUE JEU ---
    private lateinit var playerPokemon: Pokemon
    private lateinit var enemyPokemon: Pokemon

    private var isTurnInProgress = false
    private var nbPieceGagnee = 0
    private var currentTurn = 1
    private var vagueActuelle = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setupFullscreen()

        initViews()
        setupBattle()

        // --- CLIC ATTAQUER : Ouvre le DIALOG ---
        btnAttack.setOnClickListener {
            if (!isTurnInProgress) {
                // Appel du Dialog qu'on vient de créer
                val dialog = BattleAttackDialog(this, playerPokemon) { attack ->
                    // Ce code s'exécute quand on clique sur une attaque
                    executerTourDeJeu(attack)
                }
                dialog.show()
            }
        }

        // --- CLIC EQUIPE : Ouvre l'OVERLAY XML ---
        btnTeam.setOnClickListener {
            if (!isTurnInProgress) {
                afficherMenuEquipe()
            }
        }

        // --- CLIC FERMER EQUIPE ---
        btnCloseTeam.setOnClickListener {
            layoutMenuEquipe.visibility = View.GONE
        }

        return_btn.setOnClickListener {
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

    // --- GESTION ÉQUIPE (Overlay) ---
    private fun afficherMenuEquipe() {
        layoutMenuEquipe.visibility = View.VISIBLE
        teamListContainer.removeAllViews()

        for (pokemon in Player.getEquipe()) {
            val itemView = layoutInflater.inflate(R.layout.item_team_battle, teamListContainer, false)

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

            Glide.with(this).load(DataManager.model.getFrontSprite(pokemon.species.num)).into(icon)

            if (pokemon.isKO) itemView.alpha = 0.5f
            if (pokemon == playerPokemon) name.text = "${pokemon.species.nom} (Actif)"

            itemView.setOnClickListener {
                if (pokemon == playerPokemon) {
                    Toast.makeText(this, "Déjà au combat !", Toast.LENGTH_SHORT).show()
                } else if (pokemon.isKO) {
                    Toast.makeText(this, "Ce Pokémon est K.O.", Toast.LENGTH_SHORT).show()
                } else {
                    changerPokemon(pokemon)
                    layoutMenuEquipe.visibility = View.GONE
                }
            }
            teamListContainer.addView(itemView)
        }
    }

    // --- LOGIQUE COMBAT ---

    private fun setupBattle() {
        if (Player.getEquipe().isEmpty()) {
            finish()
            return
        }
        playerPokemon = Player.getPremierPokemon()

        // Sécurité : ajout d'une attaque par défaut si vide
        if (playerPokemon.attacks.isEmpty()) {
            try {
                playerPokemon.addAttack(DataManager.model.getAttackByNom("Charge"))
            } catch (e: Exception) {
                playerPokemon.addAttack(Attack(0, "Charge", "Normal", "Base", 1.0, 35, 40))
            }
        }

        if (playerPokemon.isKO) {
            playerPokemon = Player.getEquipe().firstOrNull { !it.isKO } ?: Player.getPremierPokemon()
        }

        genererEnnemi()
        updateUI()

        animateEntry(imgPokePlayer, isPlayer = true)
        animateEntry(imgPokeEnemy, isPlayer = false)

        currentTurn = 1
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        txtDialogueBattle.text = "Un ${enemyPokemon.species.nom} sauvage apparaît !"
    }

    private fun genererEnnemi() {
        val model = DataManager.model
        enemyPokemon = model.getRandomPokemon()

        val baseLvl = playerPokemon.level
        val randomLvl = (baseLvl - 2..baseLvl + 2).random().coerceAtLeast(1)

        enemyPokemon.level = 1
        for (i in 1 until randomLvl) enemyPokemon.monterLevel()
        enemyPokemon.currentHp = enemyPokemon.getMaxHp()

        val attaquesDispo = model.getAttackDispo(enemyPokemon)
        enemyPokemon.attacks.clear()
        if (attaquesDispo.isNotEmpty()) {
            val selectedAttacks = attaquesDispo.shuffled().take(4)
            for (atk in selectedAttacks) enemyPokemon.addAttack(atk)
        } else {
            try { enemyPokemon.addAttack(model.getAttackByNom("Charge")) } catch(e:Exception){}
        }

        imgPokeEnemy.alpha = 1f
        imgPokeEnemy.scaleX = 1f
        imgPokeEnemy.scaleY = 1f
    }

    private fun changerPokemon(newPokemon: Pokemon) {
        animateSwitchOut(imgPokePlayer) {
            playerPokemon = newPokemon
            updateUI()
            txtDialogueBattle.text = "Go ! ${playerPokemon.species.nom} !"
            animateEntry(imgPokePlayer, true)

            isTurnInProgress = true
            Handler(Looper.getMainLooper()).postDelayed({ tourEnnemiSeul() }, 1500)
        }
    }

    private fun executerTourDeJeu(playerAttack: Attack) {
        isTurnInProgress = true
        currentTurn++
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"

        val enemyAttack = if (enemyPokemon.attacks.isNotEmpty()) enemyPokemon.attacks.random() else Attack(0, "Lutte", "Normal", "...", 100.0, 10, 50)

        val pSpeed = playerPokemon.currentVit
        val eSpeed = enemyPokemon.currentVit
        val playerFirst = if (pSpeed == eSpeed) Random.nextBoolean() else pSpeed > eSpeed

        if (playerFirst) {
            sequenceAttaque(playerPokemon, imgPokePlayer, enemyPokemon, imgPokeEnemy, playerAttack, enemyAttack, true)
        } else {
            sequenceAttaque(enemyPokemon, imgPokeEnemy, playerPokemon, imgPokePlayer, enemyAttack, playerAttack, false)
        }
    }

    private fun sequenceAttaque(first: Pokemon, viewFirst: ImageView, second: Pokemon, viewSecond: ImageView, move1: Attack, move2: Attack, isPlayerFirst: Boolean) {
        txtDialogueBattle.text = "${first.species.nom} utilise ${move1.name} !"

        animateAttackMove(viewFirst, isPlayerFirst) {
            applicationDegats(first, second, move1)
            animateHit(viewSecond)

            if (second.isKO) {
                animateKO(viewSecond) { finDuCombat(second) }
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
                }, 2000)
            }
        }
    }

    private fun tourEnnemiSeul() {
        val enemyAttack = if (enemyPokemon.attacks.isNotEmpty()) enemyPokemon.attacks.random() else Attack(0, "Lutte", "Normal", "...", 100.0, 10, 50)
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
        val puissance = if (attaque.basePower > 0) attaque.basePower else 0
        val defense = if (defenseur.currentDef > 0) defenseur.currentDef else 1
        var degats = (puissance * attaquant.currentAtk / defense) / 2
        if (degats < 1 && puissance > 0) degats = 1
        if (puissance == 0) degats = 0

        try {
            val typeAtkEnum = PokemonType.valueOf(attaque.type.replaceFirstChar { it.uppercase() })
            val multiplicateur = PokemonType.calculerEfficaciteContre(typeAtkEnum, defenseur)
            degats = (degats * multiplicateur).toInt()
            if (multiplicateur > 1.0) {
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(this, "C'est super efficace !", Toast.LENGTH_SHORT).show()
                }, 500)
            }
        } catch (e: Exception) {}

        defenseur.prendreDmg(degats)
        updateUI()
    }

    private fun finDuCombat(loser: Pokemon) {
        if (loser == enemyPokemon) {
            txtDialogueBattle.text = "Ennemi K.O. ! Victoire !"
            nbPieceGagnee += 100

            val xpGain = enemyPokemon.level * 15
            val aLevelUp = playerPokemon.gagnerExperience(xpGain)
            var msg = "Vous gagnez $xpGain XP."
            if (aLevelUp) msg += "\n${playerPokemon.species.nom} monte au niveau ${playerPokemon.level} !"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
                vagueActuelle++
                currentTurn = 1
                numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
                setupBattle()
                isTurnInProgress = false
            }, 4000)
        } else {
            txtDialogueBattle.text = "${playerPokemon.species.nom} est K.O..."
            isTurnInProgress = false
            if (Player.getEquipe().all { it.isKO }) {
                Toast.makeText(this, "GAME OVER", Toast.LENGTH_LONG).show()
                finish()
            } else {
                afficherMenuEquipe()
            }
        }
    }

    // --- ANIMATIONS ---
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

    // --- UI ---
    private fun updateUI() {
        txtPlayerName.text = playerPokemon.species.nom
        txtPlayerLvl.text = "Lv${playerPokemon.level}"
        val maxHpP = playerPokemon.getMaxHp()
        playerHpBar.max = maxHpP
        playerHpBar.progress = playerPokemon.currentHp
        txtPlayerHpText.text = "${playerPokemon.currentHp} / $maxHpP"
        updateHpColor(playerHpBar, playerPokemon.currentHp, maxHpP)
        Glide.with(this).load(DataManager.model.getBackSprite(playerPokemon.species.num)).into(imgPokePlayer)

        txtEnemyName.text = enemyPokemon.species.nom
        txtEnemyLvl.text = "Lv${enemyPokemon.level}"
        val maxHpE = enemyPokemon.getMaxHp()
        enemyHpBar.max = maxHpE
        enemyHpBar.progress = enemyPokemon.currentHp
        updateHpColor(enemyHpBar, enemyPokemon.currentHp, maxHpE)
        Glide.with(this).load(DataManager.model.getFrontSprite(enemyPokemon.species.num)).into(imgPokeEnemy)
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