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
    private lateinit var layoutMenuEquipe: View

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
    private var isTextWriting = false
    private var nbPieceGagnee = 0
    private var currentTurn = 1
    private var vagueActuelle = 1
    val modelJson = DataManager.model
    private val VIT_TEXTE_NORMAL = 13L
    private val VIT_TEXTE_XP = 10L
    private val PAUSE_LECTURE = 500L

    // variables pour contrôler le texte asynchrone proprement
    private val textAnimHandler = Handler(Looper.getMainLooper())
    private var currentTextRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setupFullscreen()

        PokemonType.initialiserTable()

        initViews()
        setupBattle()

        btnAttack.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) {
                val dialog = BattleAttackDialog(this, playerPokemon) { attack ->
                    executerTourDeJeu(attack)
                }
                dialog.show()
            }
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
            animateEntry(imgPokePlayer, isPlayer = true)
            MusicManager.crierPokemon(playerPokemon) //cri
            animateEntry(imgPokeEnemy, isPlayer = false)
            Handler(Looper.getMainLooper()).postDelayed({
                if (estUnBoss) MusicManager.crierBoss() else MusicManager.crierPokemon(enemyPokemon) //cri
            }, 500)

        } else {
            animateEntry(imgPokeEnemy, isPlayer = false)
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
        val baseLvl = playerPokemon.level
        val randomLvl = (baseLvl - 2..baseLvl + 2).random().coerceAtLeast(1)
        enemyPokemon.level = 1
        for (i in 1 until randomLvl) enemyPokemon.monterLevel()
        enemyPokemon.currentHp = enemyPokemon.getMaxHp()

        val attaquesDispo = modelJson.getAttackDispo(enemyPokemon)
        enemyPokemon.attacks.clear()
        val selectedAttacks = attaquesDispo.shuffled().take(4)
        for (atk in selectedAttacks) enemyPokemon.addAttack(atk)
        imgPokeEnemy.alpha = 1f
        imgPokeEnemy.scaleX = 1f
        imgPokeEnemy.scaleY = 1f
    }

    private fun changerPokemon(newPokemon: Pokemon, etaitKo: Boolean) {
        isTurnInProgress = true

        animateSwitchOut(imgPokePlayer) {
            playerPokemon = newPokemon
            updateUI(animate = false)
            animateText("Go ! ${playerPokemon.species.nom} !") {
                animateEntry(imgPokePlayer, true)
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

        animateText("${first.species.nom} utilise ${move1.name} !") {

            Handler(Looper.getMainLooper()).postDelayed({
                MusicManager.jouerSonAttaque(move1.type)

                fun executerImpact1() {
                    val efficaciteMsg = applicationDegats(first, second, move1)
                    if (move1.basePower > 0) {
                        if (efficaciteMsg.contains("Coup critique !")) animateCritShake(viewSecond)
                        else animateHit(viewSecond)
                    }

                    fun suiteApresAttaque() {
                        if (second.isKO) {
                            animateKO(viewSecond) { finDuCombat(second) }
                        } else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                animateText("${second.species.nom} utilise ${move2.name} !") {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        MusicManager.jouerSonAttaque(move2.type)

                                        fun executerImpact2() {
                                            val effMsg2 = applicationDegats(second, first, move2)
                                            if (move2.basePower > 0) {
                                                if (effMsg2.contains("Coup critique !")) animateCritShake(viewFirst)
                                                else animateHit(viewFirst)
                                            }

                                            fun finDuTour() {
                                                if (first.isKO) {
                                                    animateKO(viewFirst) { finDuCombat(first) }
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
                                            animateAttackMove(viewSecond, !isPlayerFirst) { executerImpact2() }
                                        } else {
                                            Handler(Looper.getMainLooper()).postDelayed({ executerImpact2() }, 300)
                                        }

                                    }, 200)
                                }
                            }, 500)
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
                    animateAttackMove(viewFirst, isPlayerFirst) { executerImpact1() }
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
                        if (effMsg.contains("Coup critique !")) animateCritShake(imgPokePlayer)
                        else animateHit(imgPokePlayer)
                    }

                    fun checkFinTour() {
                        if (playerPokemon.isKO) {
                            animateKO(imgPokePlayer) { finDuCombat(playerPokemon) }
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
                    animateAttackMove(imgPokeEnemy, false) { executerImpactEnnemi() }
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({ executerImpactEnnemi() }, 300)
                }

            }, 200)
        }
    }

    //application des objets (restes...)
    private fun appliquerEffetsFinDeTour(pokemon: Pokemon, onTermine: () -> Unit) {
        if (pokemon.possedeObjet("item_restes") && !pokemon.isKO && pokemon.currentHp < pokemon.getMaxHp()) {
            MusicManager.jouerSonBattle("heal")
            val soinRestes = (pokemon.getMaxHp() / 16).coerceAtLeast(1)
            pokemon.heal(soinRestes)
            updateUI(true)
            animateText("${pokemon.species.nom} récupère des PV grâce aux Restes !") {
                Handler(Looper.getMainLooper()).postDelayed({ onTermine() }, 1000)
            }
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
        val statRatio = attaquant.currentAtk.toDouble() / defenseur.currentDef.toDouble()
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
            return messagesRetour // Si immunité, 0 dégâts, pas d'effets secondaires
        } else if (multiplicateur >= 2.0) {
            MusicManager.jouerSonBattle("super_eff")
            messagesRetour.add(if (multiplicateur > 2.0) "C'est extrêmement efficace !" else "C'est super efficace !")
        } else if (multiplicateur <= 0.5) {
            MusicManager.jouerSonBattle("weak_eff")
            messagesRetour.add(if (multiplicateur == 0.25) "C'est extrêmement inefficace !" else "Ce n'est pas très efficace !")
        }

        if (isCrit) messagesRetour.add("Coup critique !")

        //Application finale des dégâts
        var degatsFinal = (degats * multiplicateur).toInt()
        // sécurité pour infliger au moins 1 si ce n'est pas immunisé
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
            animateBuff(viewAtk)
        } else if (atkDebuffed) {
            MusicManager.jouerSonBattle("malus_stat_sound")
            animateDebuff(viewAtk)
        }

        //anim defenseur
        val viewDef = if (defenseur == playerPokemon) imgPokePlayer else imgPokeEnemy
        if (defDebuffed) {
            MusicManager.jouerSonBattle("malus_stat_sound")
            animateDebuff(viewDef)
        } else if (defBuffed) {
            MusicManager.jouerSonBattle("bonus_stat_sound")
            animateBuff(viewDef)
        }

        return messages
    }

    private fun appliquerStatChange(cible: Pokemon, stat: String, niveau: Int): String {
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
        return msg
    }

    fun ajouterOr(montant: Int) {
        nbPieceGagnee += montant
    }

    private fun finDuCombat(loser: Pokemon) {
        if (loser == enemyPokemon) {
            val listeDialogues = ArrayList<String>()
            listeDialogues.add("${enemyPokemon.species.nom} est K.O.")

            val xpGain = 20 + (enemyPokemon.currentAtk + enemyPokemon.currentDef + enemyPokemon.currentVit + enemyPokemon.getMaxHp())/5 + (enemyPokemon.level*20)

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
            pokemon.recalculerStats()
            pokemon.currentHp = pokemon.getMaxHp()

            for (i in pokemon.attacks.indices) {
                pokemon.currentPP[i] = pokemon.attacks[i].pp
            }
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
        MusicManager.jouerSonBattle("ko_sound")
        view.animate().scaleX(0f).scaleY(0f).alpha(0f).rotation(360f).setDuration(800).withEndAction(onEnd).start()
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

    private fun animateCritShake(view: View) {
        val shakeX = ObjectAnimator.ofFloat(view, "translationX", 0f, -30f, 30f, -20f, 20f, -10f, 10f, 0f)
        val shakeY = ObjectAnimator.ofFloat(view, "translationY", 0f, -15f, 15f, -5f, 5f, 0f)

        shakeX.duration = 400
        shakeY.duration = 400

        shakeX.start()
        shakeY.start()

        //flash blanc
        if (view is ImageView) {
            view.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
            Handler(Looper.getMainLooper()).postDelayed({
                view.clearColorFilter()
            }, 100)
        }
    }

    private fun animateBuff(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f)

        scaleX.duration = 400
        scaleY.duration = 400
        scaleX.interpolator = DecelerateInterpolator()
        scaleY.interpolator = DecelerateInterpolator()
        if (view is ImageView) {
            view.setColorFilter(android.graphics.Color.argb(100, 100, 200, 255), android.graphics.PorterDuff.Mode.SRC_ATOP)
            Handler(Looper.getMainLooper()).postDelayed({ view.clearColorFilter() }, 400)
        }

        scaleX.start()
        scaleY.start()
    }

    private fun animateDebuff(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.8f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.8f, 1.0f)

        scaleX.duration = 400
        scaleY.duration = 400
        scaleX.interpolator = DecelerateInterpolator()
        scaleY.interpolator = DecelerateInterpolator()
        if (view is ImageView) {
            view.setColorFilter(android.graphics.Color.argb(100, 150, 0, 150), android.graphics.PorterDuff.Mode.SRC_ATOP)
            Handler(Looper.getMainLooper()).postDelayed({ view.clearColorFilter() }, 400)
        }

        scaleX.start()
        scaleY.start()
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