package com.example.idlemon

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
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
    private lateinit var layoutMenuEquipe: View
    private lateinit var layoutMenuAttacks: ConstraintLayout
    private lateinit var attacksGrid: GridLayout
    private lateinit var btnReturnAttacks: ImageView

    //menue équipe
    private lateinit var teamListContainer: LinearLayout
    private lateinit var btnCloseTeam: ImageView

    //ennemi
    private lateinit var imgPokeEnemy: ImageView
    private lateinit var txtEnemyName: TextView
    private lateinit var txtEnemyLvl: TextView
    private lateinit var enemyHpBar: ProgressBar
    private lateinit var enemyItemsContainer: LinearLayout

    //player
    private lateinit var imgPokePlayer: ImageView
    private lateinit var txtPlayerName: TextView
    private lateinit var txtPlayerLvl: TextView
    private lateinit var playerHpBar: ProgressBar
    private lateinit var txtPlayerHpText: TextView
    private lateinit var playerItemsContainer: LinearLayout

    //btn
    private lateinit var btnAttack: ConstraintLayout
    private lateinit var btnTeam: ConstraintLayout

    //logique jeu
    private lateinit var playerPokemon: Pokemon
    private lateinit var enemyPokemon: Pokemon

    private var isTurnInProgress = false
    private var isTextWriting = false
    private var nbPieceGagnee = 0
    private var currentTurn = 1
    private var vagueActuelle = 1
    val modelJson = DataManager.model
    private var VIT_TEXTE_NORMAL = 8L
    private var VIT_TEXTE_XP = 3L
    private var PAUSE_LECTURE = 100L

    //variables pour contrôler le texte asynchrone proprement
    private val textAnimHandler = Handler(Looper.getMainLooper())
    private var currentTextRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setupFullscreen()

        //param vitesse dialogues
        if (SettingsManager.isFastDialogue(this)) {
            VIT_TEXTE_NORMAL = 2L
            VIT_TEXTE_XP = 1L
            PAUSE_LECTURE = 30L
        } else {
            VIT_TEXTE_NORMAL = 8L
            VIT_TEXTE_XP = 3L
            PAUSE_LECTURE = 100L
        }

        PokemonType.initialiserTable()

        initViews()
        setupBattle()

        btnAttack.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) {
                layoutMenuPrincipal.visibility = View.GONE
                layoutMenuAttacks.visibility = View.VISIBLE
                afficherAttaques(playerPokemon)
            }
        }

        btnReturnAttacks.setOnClickListener {
            layoutMenuAttacks.visibility = View.GONE
            layoutMenuPrincipal.visibility = View.VISIBLE
        }

        btnTeam.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) {
                afficherMenuEquipe(false)
            }
        }

        btnCloseTeam.setOnClickListener {
            //si le poké actif est ko, on force le changement de poké
            if(playerPokemon.isKO){
                Toast.makeText(this, "Veuillez changer de pokémon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                layoutMenuEquipe.visibility = View.GONE
            }
        }

        return_btn.setOnClickListener {
            val butinTotal = nbPieceGagnee
            Player.addPieces(butinTotal)
            Toast.makeText(this, "Vous emportez $butinTotal PokéOr !", Toast.LENGTH_SHORT).show()

            MusicManager.jouerPlaylistHome(this)
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

        layoutMenuAttacks = findViewById(R.id.layout_menu_attacks)
        attacksGrid = findViewById(R.id.attacks_grid)
        btnReturnAttacks = findViewById(R.id.btn_return_attacks)

        teamListContainer = findViewById(R.id.team_list_container)
        btnCloseTeam = findViewById(R.id.btn_close_team)

        imgPokeEnemy = findViewById(R.id.imgPokeEnemy)
        txtEnemyName = findViewById(R.id.txt_enemy_name)
        txtEnemyLvl = findViewById(R.id.txt_enemy_lvl)
        enemyHpBar = findViewById(R.id.enemy_hp_bar)
        enemyItemsContainer = findViewById(R.id.enemy_items_container)

        imgPokePlayer = findViewById(R.id.imgPokePlayer)
        txtPlayerName = findViewById(R.id.txt_player_name)
        txtPlayerLvl = findViewById(R.id.txt_player_lvl)
        playerHpBar = findViewById(R.id.player_hp_bar)
        txtPlayerHpText = findViewById(R.id.txt_player_hp_text)
        playerItemsContainer = findViewById(R.id.player_items_container)

        btnAttack = findViewById(R.id.btn_attack_container)
        btnTeam = findViewById(R.id.btn_team_container)
    }

    private fun afficherAttaques(pokemon: Pokemon) {
        attacksGrid.removeAllViews()

        for (attack in pokemon.attacks) {
            val attackView = LayoutInflater.from(this).inflate(R.layout.item_attack_battle, attacksGrid, false)

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            attackView.layoutParams = params

            val txtNom = attackView.findViewById<TextView>(R.id.nomAttack)
            val txtPp = attackView.findViewById<TextView>(R.id.ppMaxTextView)
            val rootLayout = attackView.findViewById<ConstraintLayout>(R.id.attack_item_root)

            txtNom.text = attack.name

            val attackIndex = pokemon.attacks.indexOf(attack)
            val currentPP = pokemon.currentPP[attackIndex] ?: attack.pp

            txtPp.text = "$currentPP/${attack.pp}"

            rootLayout.backgroundTintList = ColorStateList.valueOf(getColorForType(attack.type))

            if (currentPP == 0) {
                attackView.alpha = 0.5f
            } else {
                attackView.alpha = 1.0f
            }

            attackView.setOnClickListener {
                if (currentPP > 0) {
                    layoutMenuAttacks.visibility = View.GONE
                    layoutMenuPrincipal.visibility = View.VISIBLE

                    executerTourDeJeu(attack)
                } else {
                    Toast.makeText(this, "Plus de PP pour cette attaque !", Toast.LENGTH_SHORT).show()
                }
            }

            attacksGrid.addView(attackView)
        }
    }

    private fun afficherMenuEquipe(etaitKo: Boolean) {
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
                    changerPokemon(pokemon, etaitKo)
                    layoutMenuEquipe.visibility = View.GONE
                }
            }
            teamListContainer.addView(itemView)
        }
    }

    private fun setupBattle() {
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

        val estUnBoss = vagueActuelle % 10 == 0

        if (estUnBoss) {
            MusicManager.jouerPlaylistBoss(this)
            genererEnnemi()
        } else {
            genererEnnemi()
        }

        if (vagueActuelle > 1 && (vagueActuelle - 1) % 10 == 0) {
            MusicManager.jouerPlaylistBattle(this)
            soignerTout()
        }
        updateUI(animate = false)

        if (vagueActuelle == 1) {
            BattleAnimator.animateEntry(imgPokePlayer, isPlayer = true)
            MusicManager.crierPokemon(playerPokemon) //cri
            BattleAnimator.animateEntry(imgPokeEnemy, isPlayer = false)
            Handler(Looper.getMainLooper()).postDelayed({
                if (estUnBoss) MusicManager.crierBoss() else MusicManager.crierPokemon(enemyPokemon) //cri
            }, 500)

        } else {
            BattleAnimator.animateEntry(imgPokeEnemy, isPlayer = false)
            if (estUnBoss) MusicManager.crierBoss() else MusicManager.crierPokemon(enemyPokemon) //cri Boss
        }

        currentTurn = 1
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"

        animateText("Un ${enemyPokemon.species.nom} sauvage apparaît !")
    }

    private fun soignerTout() {
        for (pokemon in Player.getEquipe()) {
            pokemon.heal(pokemon.getMaxHp())
            for (i in pokemon.attacks.indices) {
                pokemon.currentPP[i] = pokemon.attacks[i].pp
            }
        }
    }

    private fun genererEnnemi() {
        enemyPokemon = modelJson.getRandomPokemon()
        val estUnBoss = vagueActuelle % 10 == 0

        val baseLvl = vagueActuelle
        var randomLvl = (baseLvl - 1..baseLvl + 1).random().coerceAtLeast(1)

        if (estUnBoss) {
            randomLvl += (3..5).random()
            imgPokeEnemy.scaleX = 1.4f
            imgPokeEnemy.scaleY = 1.4f
        } else {
            imgPokeEnemy.scaleX = 1f
            imgPokeEnemy.scaleY = 1f
        }

        //monte le poké de level
        enemyPokemon.level = 1
        for (i in 1 until randomLvl) enemyPokemon.monterLevel()

        val tousLesObjets = listOf(
            "atk_plus", "def_plus", "vit_plus", "pv_plus",
            "item_restes", "item_bague_force", "item_veste_combat", "item_cape_vitesse"
        )

        val nbObjets: Int
        if (estUnBoss) {
            //Boss gagne 1 objet toutes les 2 ou 3 vagues, avec un minimum garanti
            val minObj = (vagueActuelle / 4).coerceAtLeast(1)
            val maxObj = (vagueActuelle / 2).coerceAtLeast(2)
            nbObjets = (minObj..maxObj).random()
        } else {
            val minObj = enemyPokemon.level / 10
            val maxObj = enemyPokemon.level / 5
            nbObjets = if (minObj <= maxObj) (minObj..maxObj).random() else minObj
        }

        //ajout objet random
        repeat(nbObjets) {
            enemyPokemon.ajouterObjet(tousLesObjets.random())
        }
        enemyPokemon.currentHp = enemyPokemon.getMaxHp()

        imgPokeEnemy.alpha = 1f
    }

    private fun changerPokemon(newPokemon: Pokemon, etaitKo: Boolean) {
        isTurnInProgress = true

        //reset stage hors combats
        playerPokemon.resetStagesCombat()

        BattleAnimator.animateSwitchOut(imgPokePlayer) {
            playerPokemon = newPokemon
            updateUI(animate = false)
            animateText("Go ! ${playerPokemon.species.nom} !") {
                BattleAnimator.animateEntry(imgPokePlayer, true)
                MusicManager.crierPokemon(playerPokemon) //cri Poké

                Handler(Looper.getMainLooper()).postDelayed({
                    if(etaitKo) {
                        isTurnInProgress = false
                        animateText("Que doit faire ${playerPokemon.species.nom} ?")
                    } else {
                        tourEnnemiSeul()
                    }
                }, 1000)
            }
        }
    }

    private fun executerTourDeJeu(playerAttack: Attack) {
        isTurnInProgress = true
        currentTurn++
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        val enemyAttack = enemyPokemon.attacks.random()

        // On utilise battleVit pour prendre en compte les buffs/malus de vitesse
        val pSpeed = playerPokemon.battleVit
        val eSpeed = enemyPokemon.battleVit
        val playerFirst = if (pSpeed == eSpeed) Random.nextBoolean() else pSpeed > eSpeed

        if (playerFirst) {
            sequenceAttaque(playerPokemon, imgPokePlayer, enemyPokemon, imgPokeEnemy, playerAttack, enemyAttack, true)
        } else {
            sequenceAttaque(enemyPokemon, imgPokeEnemy, playerPokemon, imgPokePlayer, enemyAttack, playerAttack, false)
        }
    }

    private fun sequenceAttaque(first: Pokemon, viewFirst: ImageView, second: Pokemon, viewSecond: ImageView, move1: Attack, move2: Attack, isPlayerFirst: Boolean) {

        animateText("${first.species.nom} utilise ${move1.name} !") {

            Handler(Looper.getMainLooper()).postDelayed({
                MusicManager.jouerSonAttaque(move1.type)

                fun executerImpact1() {
                    val efficaciteMsg = applicationDegats(first, second, move1)
                    if (move1.basePower > 0) {
                        if (efficaciteMsg.contains("Coup critique !")) BattleAnimator.animateCritShake(viewSecond)
                        else BattleAnimator.animateHit(viewSecond)
                    }

                    fun suiteApresAttaque() {
                        if (second.isKO) {
                            BattleAnimator.animateKO(viewSecond, second) { finDuCombat(second) }
                        } else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                animateText("${second.species.nom} utilise ${move2.name} !") {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        MusicManager.jouerSonAttaque(move2.type)

                                        fun executerImpact2() {
                                            val effMsg2 = applicationDegats(second, first, move2)
                                            if (move2.basePower > 0) {
                                                if (effMsg2.contains("Coup critique !")) BattleAnimator.animateCritShake(viewFirst)
                                                else BattleAnimator.animateHit(viewFirst)
                                            }

                                            fun finDuTour() {
                                                if (first.isKO) {
                                                    BattleAnimator.animateKO(viewFirst, first) { finDuCombat(first) }
                                                } else {
                                                    appliquerEffetsFinDeTour(first) {
                                                        appliquerEffetsFinDeTour(second) {
                                                            isTurnInProgress = false
                                                            animateText("Que doit faire ${playerPokemon.species.nom} ?")
                                                        }
                                                    }
                                                }
                                            }

                                            if (effMsg2.isNotEmpty()) {
                                                afficherDialoguesSuccessifs(effMsg2) {
                                                    Handler(Looper.getMainLooper()).postDelayed({ finDuTour() }, PAUSE_LECTURE)
                                                }
                                            } else {
                                                finDuTour()
                                            }
                                        }

                                        if (move2.basePower > 0) {
                                            BattleAnimator.animateAttackMove(viewSecond, !isPlayerFirst) { executerImpact2() }
                                        } else {
                                            Handler(Looper.getMainLooper()).postDelayed({ executerImpact2() }, 300)
                                        }

                                    }, 200)
                                }
                            }, 1200)
                        }
                    }

                    if (efficaciteMsg.isNotEmpty()) {
                        afficherDialoguesSuccessifs(efficaciteMsg) {
                            Handler(Looper.getMainLooper()).postDelayed({ suiteApresAttaque() }, PAUSE_LECTURE)
                        }
                    } else {
                        suiteApresAttaque()
                    }
                }

                if (move1.basePower > 0) {
                    BattleAnimator.animateAttackMove(viewFirst, isPlayerFirst) { executerImpact1() }
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({ executerImpact1() }, 300)
                }

            }, 200)
        }
    }

    private fun tourEnnemiSeul() {
        if(playerPokemon.isKO) {
            isTurnInProgress = false
            return
        }

        val enemyAttack = enemyPokemon.attacks.random()

        animateText("${enemyPokemon.species.nom} utilise ${enemyAttack.name} !") {

            Handler(Looper.getMainLooper()).postDelayed({
                MusicManager.jouerSonAttaque(enemyAttack.type)

                fun executerImpactEnnemi() {
                    val effMsg = applicationDegats(enemyPokemon, playerPokemon, enemyAttack)
                    if (enemyAttack.basePower > 0) {
                        if (effMsg.contains("Coup critique !")) BattleAnimator.animateCritShake(imgPokePlayer)
                        else BattleAnimator.animateHit(imgPokePlayer)
                    }

                    fun checkFinTour() {
                        if (playerPokemon.isKO) {
                            BattleAnimator.animateKO(imgPokePlayer, playerPokemon) { finDuCombat(playerPokemon) }
                        } else {
                            appliquerEffetsFinDeTour(enemyPokemon) {
                                appliquerEffetsFinDeTour(playerPokemon) {
                                    isTurnInProgress = false
                                    animateText("Que doit faire ${playerPokemon.species.nom} ?")
                                }
                            }
                        }
                    }

                    if (effMsg.isNotEmpty()) {
                        afficherDialoguesSuccessifs(effMsg) {
                            Handler(Looper.getMainLooper()).postDelayed({ checkFinTour() }, PAUSE_LECTURE)
                        }
                    } else {
                        checkFinTour()
                    }
                }

                if (enemyAttack.basePower > 0) {
                    BattleAnimator.animateAttackMove(imgPokeEnemy, false) { executerImpactEnnemi() }
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({ executerImpactEnnemi() }, 300)
                }

            }, 200)
        }
    }

    //application des objets (restes...)
    private fun appliquerEffetsFinDeTour(pokemon: Pokemon, onTermine: () -> Unit) {
        val quantiteRestes = (pokemon.objets["item_restes"] ?: 0).coerceAtMost(6)

        if (quantiteRestes > 0 && !pokemon.isKO && pokemon.currentHp < pokemon.getMaxHp()) {
            MusicManager.jouerSonBattle("item_active")
            val soinRestes = ((pokemon.getMaxHp() * quantiteRestes) / 16).coerceAtLeast(1)
            pokemon.heal(soinRestes)
            updateUI(true)

            Handler(Looper.getMainLooper()).postDelayed({ onTermine() }, 500)
        } else {
            onTermine()
        }
    }

    private fun applicationDegats(attaquant: Pokemon, defenseur: Pokemon, attaque: Attack): List<String> {
        val messagesRetour = mutableListOf<String>()
        //pp
        val atkIndex = attaquant.attacks.indexOf(attaque)
        if (atkIndex != -1) {
            val currentPP = attaquant.currentPP[atkIndex] ?: attaque.pp
            if (currentPP > 0) attaquant.currentPP[atkIndex] = currentPP - 1
        }

        //ATTAQUES STATUS
        if (attaque.basePower == 0) {
            //Soin direct
            if (attaque.heal > 0) {
                MusicManager.jouerSonBattle("heal")
                val healAmount = (attaquant.getMaxHp() * attaque.heal) / 100
                attaquant.heal(healAmount)
                updateUI(animate = true)
            }
            //Bonus/Malus
            messagesRetour.addAll(appliquerEffetsStats(attaquant, defenseur, attaque))

            //rien
            if (messagesRetour.isEmpty() && attaque.heal == 0) {
                messagesRetour.add("Mais il ne se passe rien...")
            }
            //on s'arrete là, pas besoin de calculer l'efficacité d'un buff ou heal
            return messagesRetour
        }

        //ATTAQUES AVEC DÉGÂTS

        //calcul puissance brute
        val levelFactor = (2 * attaquant.level / 5) + 2

        // On utilise battleAtk et battleDef pour prendre en compte les buffs/malus
        val statRatio = attaquant.battleAtk.toDouble() / defenseur.battleDef.toDouble()
        var degats = (((levelFactor * attaque.basePower * statRatio) / 50) + 2).toDouble()

        //safe si dmg tres bas
        if (degats < 1) degats = 1.0

        //gestion miss et esquive
        val isMiss = Random.nextDouble() >= attaque.accuracy
        if(isMiss){
            messagesRetour.add("${defenseur.species.nom} évite l'attaque !")
            return messagesRetour //ahh il a raté le looser
        }

        //gestion critique
        val isCrit = Random.nextDouble() < attaque.critRatio
        if (isCrit) degats *= 1.5

        //calcul efficacité des types
        val typeAtkEnum = try { PokemonType.valueOf(attaque.type.uppercase()) } catch (e: Exception) { PokemonType.NORMAL }
        val multiplicateur = PokemonType.calculerEfficaciteContre(typeAtkEnum, defenseur)

        if (multiplicateur == 0.0) {
            messagesRetour.add("Ça n'affecte pas ${defenseur.species.nom}...")
            return messagesRetour //si immunité, 0 dégâts et pas d'effets
        } else if (multiplicateur >= 2.0) {
            MusicManager.jouerSonBattle("super_eff")
            messagesRetour.add(if (multiplicateur > 2.0) "C'est extrêmement efficace !" else "C'est super efficace !")
        } else if (multiplicateur <= 0.5) {
            MusicManager.jouerSonBattle("weak_eff")
            messagesRetour.add(if (multiplicateur == 0.25) "C'est extrêmement inefficace !" else "Ce n'est pas très efficace !")
        }

        if (isCrit) messagesRetour.add("Coup critique !")

        //Application finale des dégâts
        val estStabbe = attaquant.species.type.any { it.nom.equals(attaque.type, ignoreCase = true) }
        val stabMultiplicateur = if (estStabbe) 1.3 else 1.0 //attaque stabbée

        var degatsDouble = degats * multiplicateur * stabMultiplicateur
        var degatsFinal = degatsDouble.toInt()

        //save si attaque est très basse
        if (degatsFinal < 1) degatsFinal = 1

        defenseur.prendreDmg(degatsFinal)

        //Bruitage Alarme PV Bas
        if (defenseur == playerPokemon && !defenseur.isKO && (defenseur.currentHp.toFloat() / defenseur.getMaxHp()) <= 0.2f) {
            MusicManager.jouerSonBattle("low_hp")
        }

        //Vol de vie
        if (attaque.drain && degatsFinal > 0) {
            val drainAmount = degatsFinal / 2
            if (drainAmount > 0) {
                MusicManager.jouerSonBattle("heal")
                attaquant.heal(drainAmount)
            }
        }

        //bonus_malus associés à l'attaque
        messagesRetour.addAll(appliquerEffetsStats(attaquant, defenseur, attaque))
        updateUI(animate = true)

        return messagesRetour
    }

    private fun appliquerEffetsStats(attaquant: Pokemon, defenseur: Pokemon, attaque: Attack): List<String> {
        val messages = mutableListOf<String>()
        var atkBuffed = false
        var atkDebuffed = false
        var defBuffed = false
        var defDebuffed = false

        //bonus
        attaque.bonus?.forEach { map ->
            map.forEach { (stat, valeur) ->
                val msg = appliquerStatChange(attaquant, stat, valeur)
                if (msg.isNotEmpty()) {
                    messages.add(msg)
                    if (valeur > 0) atkBuffed = true else atkDebuffed = true
                }
            }
        }

        //malus
        attaque.malus?.forEach { map ->
            map.forEach { (stat, valeur) ->
                val msg = appliquerStatChange(defenseur, stat, valeur)
                if (msg.isNotEmpty()) {
                    messages.add(msg)
                    if (valeur > 0) defBuffed = true else defDebuffed = true
                }
            }
        }

        //anim attaquant
        val viewAtk = if (attaquant == playerPokemon) imgPokePlayer else imgPokeEnemy
        if (atkBuffed) {
            MusicManager.jouerSonBattle("bonus_stat_sound")
            BattleAnimator.animateBuff(viewAtk)
        } else if (atkDebuffed) {
            MusicManager.jouerSonBattle("malus_stat_sound")
            BattleAnimator.animateDebuff(viewAtk)
        }

        //anim defenseur
        val viewDef = if (defenseur == playerPokemon) imgPokePlayer else imgPokeEnemy
        if (defDebuffed) {
            MusicManager.jouerSonBattle("malus_stat_sound")
            BattleAnimator.animateDebuff(viewDef)
        } else if (defBuffed) {
            MusicManager.jouerSonBattle("bonus_stat_sound")
            BattleAnimator.animateBuff(viewDef)
        }

        return messages
    }

    private fun appliquerStatChange(cible: Pokemon, stat: String, niveau: Int): String {
        // Appelle la nouvelle mécanique de paliers définie dans Pokemon.kt
        return cible.modifierStage(stat, niveau)
    }

    fun ajouterOr(montant: Int) {
        nbPieceGagnee += montant
    }

    private fun finDuCombat(loser: Pokemon) {
        if (loser == enemyPokemon) {
            val listeDialogues = ArrayList<String>()
            listeDialogues.add("${enemyPokemon.species.nom} est K.O.")

            val xpGain = 20 + (enemyPokemon.currentAtk + enemyPokemon.currentDef + enemyPokemon.currentVit + enemyPokemon.getMaxHp())/5 + (enemyPokemon.level*50)

            for(poke in Player.getEquipe()){
                if(!poke.isKO){
                    val gainReel = if (poke == playerPokemon) xpGain else xpGain/3
                    val aLevelUp = poke.gagnerExperience(gainReel)

                    var msgXP = "${poke.species.nom} gagne $gainReel XP"
                    if (aLevelUp) {
                        msgXP += ", il passe au niveau ${poke.level}."
                    } else {
                        msgXP += "."
                    }
                    listeDialogues.add(msgXP)
                    if (aLevelUp){
                        if(poke.species.evoLevel != null && poke.species.evoLevel!! <= poke.level){
                            Toast.makeText(this, "EVOLUTIONN", Toast.LENGTH_SHORT).show()
                            listeDialogues.add("Hein ? ${poke.species.nom} évolue !")
                            val oldName = poke.species.nom
                            val oldLevel = poke.level
                            poke.species = modelJson.creerPokemon(poke.species.evo).species
                            poke.level = 1
                            for (i in 1 until oldLevel) poke.monterLevel()
                            poke.currentHp = poke.getMaxHp()

                            listeDialogues.add("$oldName a évolué en ${poke.species.nom} !")
                        }
                    }
                }
            }

            afficherDialoguesSuccessifs(listeDialogues) {
                if (!isDestroyed && !isFinishing) {
                    val rewardDialog = RewardBattleVague(this, playerPokemon) { messagesReward ->
                        afficherDialoguesSuccessifs(messagesReward) {
                            // On réinitialise les stats de ton Pokémon à la fin de la vague
                            playerPokemon.resetStagesCombat()

                            vagueActuelle++
                            currentTurn = 1
                            numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
                            updateUI(animate = false)
                            setupBattle()
                            isTurnInProgress = false
                        }
                    }
                    rewardDialog.show()
                }
            }

        } else {
            animateText("${playerPokemon.species.nom} est K.O...") {
                isTurnInProgress = false
                if (Player.getEquipe().all { it.isKO }) {
                    animateText("GAME OVER - Butin : $nbPieceGagnee Or") {
                        Player.addPieces(nbPieceGagnee)
                        resetPartieDonnees()
                        MusicManager.jouerPlaylistHome(this)
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 800)
                    }
                } else {
                    afficherMenuEquipe(true)
                }
            }
        }
    }

    private fun afficherDialoguesSuccessifs(messages: List<String>, onFinished: () -> Unit) {
        if (messages.isEmpty()) {
            onFinished()
            return
        }

        val messageActuel = messages[0]
        val isXpMsg = messageActuel.contains("XP") || messageActuel.contains("niveau") || messageActuel.contains("gagne")
        val vitesse = if (isXpMsg) VIT_TEXTE_XP else VIT_TEXTE_NORMAL

        if (messageActuel.contains("passe au niveau")) {
            MusicManager.sonLevelUpPoke()
        } else if (messageActuel.contains("évolué")) {
            MusicManager.sonEvoPoke()
        }

        animateText(messageActuel, vitesse) {
            Handler(Looper.getMainLooper()).postDelayed({
                afficherDialoguesSuccessifs(messages.drop(1), onFinished)
            }, PAUSE_LECTURE)
        }
    }

    private fun animateText(text: String, speed: Long = VIT_TEXTE_NORMAL, onCompleted: (() -> Unit)? = null) {
        currentTextRunnable?.let { textAnimHandler.removeCallbacks(it) }

        isTextWriting = true
        txtDialogueBattle.text = ""
        var charIndex = 0

        currentTextRunnable = object : Runnable {
            override fun run() {
                if(isDestroyed || isFinishing) return

                if (charIndex < text.length) {
                    txtDialogueBattle.append(text[charIndex].toString())
                    charIndex++
                    textAnimHandler.postDelayed(this, speed)
                } else {
                    isTextWriting = false
                    currentTextRunnable = null
                    onCompleted?.invoke()
                }
            }
        }
        textAnimHandler.post(currentTextRunnable!!)
    }

    fun resetPartieComplet(){
        resetPartieDonnees()
        vagueActuelle = 1
        currentTurn = 1
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        isTurnInProgress = false
        animateText("Un ${enemyPokemon.species.nom} sauvage apparaît !")
        updateUI(animate = false)
    }

    private fun resetPartieDonnees() {
        nbPieceGagnee = 0
        vagueActuelle = 1
        currentTurn = 1

        for (pokemon in Player.getEquipe()) {
            pokemon.species = pokemon.originalSpecies

            pokemon.objets.clear()
            pokemon.isKO = false
            pokemon.level = 1
            pokemon.exp = 0

            // Remise à zéro des stages lors d'une nouvelle partie
            pokemon.resetStagesCombat()

            pokemon.recalculerStats()
            pokemon.currentHp = pokemon.getMaxHp()

            for (i in pokemon.attacks.indices) {
                pokemon.currentPP[i] = pokemon.attacks[i].pp
            }
        }
    }

    //UI
    private fun updateUI(animate: Boolean) {
        if(isDestroyed || isFinishing) return

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

        // MAJ des objets équipés
        afficherObjets(playerPokemon, playerItemsContainer)
        afficherObjets(enemyPokemon, enemyItemsContainer)
    }

    private fun afficherObjets(pokemon: Pokemon, container: LinearLayout) {
        container.removeAllViews()
        pokemon.objets.forEach { (idObjet, quantite) ->
            if (quantite > 0) {
                val frame = FrameLayout(this)
                val density = resources.displayMetrics.density
                val sizePx = (24 * density).toInt()
                val marginPx = (4 * density).toInt()

                val frameParams = LinearLayout.LayoutParams(sizePx, sizePx)
                frameParams.setMargins(0, 0, marginPx, 0)
                frame.layoutParams = frameParams

                val img = ImageView(this)
                img.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                img.setImageResource(getIconForObjet(idObjet))
                frame.addView(img)

                if (quantite > 1) {
                    val txt = TextView(this)
                    val txtParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    txtParams.gravity = Gravity.BOTTOM or Gravity.END
                    txt.layoutParams = txtParams
                    txt.text = "x$quantite"
                    txt.textSize = 10f
                    txt.setTextColor(Color.WHITE)
                    txt.setShadowLayer(3f, 1f, 1f, Color.BLACK)
                    txt.setTypeface(null, Typeface.BOLD)
                    frame.addView(txt)
                }

                container.addView(frame)
            }
        }
    }

    private fun getIconForObjet(id: String): Int {
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

    private fun getColorForType(type: String): Int {
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
}