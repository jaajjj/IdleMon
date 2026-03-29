package com.example.idlemon

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.activity.OnBackPressedCallback
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

    //menu principal
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
    private lateinit var enemyInfoContainer: ConstraintLayout

    //player
    private lateinit var imgPokePlayer: ImageView
    private lateinit var txtPlayerName: TextView
    private lateinit var txtPlayerLvl: TextView
    private lateinit var playerHpBar: ProgressBar
    private lateinit var txtPlayerHpText: TextView
    private lateinit var playerItemsContainer: LinearLayout
    private lateinit var playerInfoContainer: ConstraintLayout

    //btn actions
    private lateinit var btnAttack: ConstraintLayout
    private lateinit var btnTeam: ConstraintLayout

    //dialog stats
    private lateinit var statsButtonEnemy: ImageView
    private lateinit var statsButtonPlayer: ImageView

    //dialog quitter
    private lateinit var quitDialog: Dialog

    //logique jeu
    private lateinit var playerPokemon: Pokemon
    private lateinit var enemyPokemon: Pokemon

    //flags de controle
    private var isTurnInProgress = false
    private var isTextWriting = false
    private var nbPieceGagnee = 0
    private var currentTurn = 1
    private var vagueActuelle = 1
    val modelJson = DataManager.model

    //vitesses de texte par defaut
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

        //reset à chaque début de game par sécurité
        resetPartieDonnees()

        //interception du bouton "Retour" d'Android
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isTurnInProgress && !isTextWriting) {
                    quitDialog.findViewById<TextView>(R.id.tvQuitLoot).text = "Butin : $nbPieceGagnee PokéOr"
                    quitDialog.show()
                }
            }
        })

        //param vitesse dialogues selon les settings
        if (SettingsManager.isFastDialogue(this)) {
            VIT_TEXTE_NORMAL = 2L
            VIT_TEXTE_XP = 1L
            PAUSE_LECTURE = 50L
        } else {
            VIT_TEXTE_NORMAL = 15L
            VIT_TEXTE_XP = 5L
            PAUSE_LECTURE = 250L
        }

        //init la table de type de PokemonType.kt
        PokemonType.initialiserTable()

        //init des vues xml et de tous leurs événements (clics)
        initViews()

        //init battle (spawn des pokémon)
        setupBattle()
    }

    private fun quitterPartie() {
        //sauvegarde et affiche le butin
        val butinTotal = nbPieceGagnee
        Player.addPieces(butinTotal)
        Toast.makeText(this, "Vous emportez $butinTotal PokéOr !", Toast.LENGTH_SHORT).show()

        MusicManager.jouerPlaylistHome(this)
        resetPartieDonnees()
        finish()
    }

    private fun initViews() {
        //bind les textviews du haut
        txtDialogueBattle = findViewById(R.id.txt_dialogue_battle)
        return_btn = findViewById(R.id.return_btn)
        numVague = findViewById(R.id.numVague)

        //bind les menus
        layoutMenuPrincipal = findViewById(R.id.layout_menu_principal)
        layoutMenuEquipe = findViewById(R.id.layout_menu_equipe)
        layoutMenuAttacks = findViewById(R.id.layout_menu_attacks)
        attacksGrid = findViewById(R.id.attacks_grid)
        btnReturnAttacks = findViewById(R.id.btn_return_attacks)
        teamListContainer = findViewById(R.id.team_list_container)
        btnCloseTeam = findViewById(R.id.btn_close_team)

        //poké enemy
        imgPokeEnemy = findViewById(R.id.imgPokeEnemy)
        txtEnemyName = findViewById(R.id.txt_enemy_name)
        txtEnemyLvl = findViewById(R.id.txt_enemy_lvl)
        enemyHpBar = findViewById(R.id.enemy_hp_bar)
        enemyItemsContainer = findViewById(R.id.enemy_items_container)
        enemyInfoContainer = findViewById(R.id.enemy_info_container)
        statsButtonEnemy = findViewById(R.id.statsButtonEnemy)

        //poké player
        imgPokePlayer = findViewById(R.id.imgPokePlayer)
        txtPlayerName = findViewById(R.id.txt_player_name)
        txtPlayerLvl = findViewById(R.id.txt_player_lvl)
        playerHpBar = findViewById(R.id.player_hp_bar)
        txtPlayerHpText = findViewById(R.id.txt_player_hp_text)
        playerItemsContainer = findViewById(R.id.player_items_container)
        playerInfoContainer = findViewById(R.id.player_info_container)
        statsButtonPlayer = findViewById(R.id.statsButtonPlayer)

        btnAttack = findViewById(R.id.btn_attack_container)
        btnTeam = findViewById(R.id.btn_team_container)

        //dialog quitter
        quitDialog = Dialog(this)
        quitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        quitDialog.setContentView(R.layout.dialog_quit_battle)
        quitDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //responsive width a 90%
        val widthQuit = (resources.displayMetrics.widthPixels * 0.90).toInt()
        quitDialog.window?.setLayout(widthQuit, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        // C'EST ICI LA CORRECTION : on cherche bien dans quitDialog
        quitDialog.findViewById<ImageView>(R.id.quitCloseBtn)?.setOnClickListener { quitDialog.dismiss() } //annule la fuite
        quitDialog.findViewById<View>(R.id.btnConfirmQuit)?.setOnClickListener {
            quitDialog.dismiss()
            quitterPartie()
        }

        //--Listener--
        //click bouton attaque principal
        btnAttack.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) {
                layoutMenuPrincipal.visibility = View.GONE
                layoutMenuAttacks.visibility = View.VISIBLE
                if(::playerPokemon.isInitialized) afficherAttaques(playerPokemon)
            }
        }

        //retour menu depuis grid
        btnReturnAttacks.setOnClickListener {
            layoutMenuAttacks.visibility = View.GONE
            layoutMenuPrincipal.visibility = View.VISIBLE
        }

        //click bouton equipe
        btnTeam.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) afficherMenuEquipe(false)
        }

        //fermer le menu equipe
        btnCloseTeam.setOnClickListener {
            //si le poké actif est ko, on force le changement de poké
            if(::playerPokemon.isInitialized && playerPokemon.isKO){
                Toast.makeText(this, "Veuillez changer de pokémon", Toast.LENGTH_SHORT).show()
            } else {
                layoutMenuEquipe.visibility = View.GONE
            }
        }

        //click sur retour pour quitter (popup)
        return_btn.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) {
                quitDialog.findViewById<TextView>(R.id.tvQuitLoot).text = "Butin : $nbPieceGagnee PokéOr"
                quitDialog.show()
            }
        }

        //stats click listener
        statsButtonPlayer.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) PokemonStatsDialog(this).show(playerPokemon)
        }
        statsButtonEnemy.setOnClickListener {
            if (!isTurnInProgress && !isTextWriting) PokemonStatsDialog(this).show(enemyPokemon)
        }
    }

    private fun afficherAttaques(pokemon: Pokemon) {
        attacksGrid.removeAllViews()

        //récupere la vue attack pour chaque attack du poké
        for (attack in pokemon.attacks) {
            val attackView = LayoutInflater.from(this).inflate(R.layout.item_attack_battle, attacksGrid, false)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            attackView.layoutParams = params

            //init vues dislog
            val txtNom = attackView.findViewById<TextView>(R.id.nomAttack)
            val txtPp = attackView.findViewById<TextView>(R.id.ppMaxTextView)
            val rootLayout = attackView.findViewById<ConstraintLayout>(R.id.attack_item_root)

            txtNom.text = attack.name

            //calcul pp
            val attackIndex = pokemon.attacks.indexOf(attack)
            val currentPP = pokemon.currentPP[attackIndex] ?: attack.pp
            txtPp.text = "$currentPP/${attack.pp}"

            rootLayout.backgroundTintList = ColorStateList.valueOf(UIHelper.getColorForType(attack.type))

            //grise si vide
            if (currentPP == 0) attackView.alpha = 0.5f else attackView.alpha = 1.0f

            //si pp, gérer l'attaque, sinon rien
            attackView.setOnClickListener {
                if (currentPP > 0) {
                    layoutMenuAttacks.visibility = View.GONE
                    layoutMenuPrincipal.visibility = View.VISIBLE
                    executerTourDeJeu(attack)
                }
            }
            //on ajoute l'attaque à la grille
            attacksGrid.addView(attackView)
        }
    }

    fun afficherMenuEquipe(etaitKo: Boolean, modeSelectionObjet: Boolean = false, onPokemonSelected: ((Pokemon) -> Unit)? = null) {
        layoutMenuEquipe.visibility = View.VISIBLE
        teamListContainer.removeAllViews()

        //en mode sélection on cache la croix pour obliger le joueur à choisir
        btnCloseTeam.visibility = if (modeSelectionObjet) View.GONE else View.VISIBLE

        for (pokemon in Player.getEquipe()) {
            val itemView = layoutInflater.inflate(R.layout.item_team_battle, teamListContainer, false)

            //recup les ids de l'item
            val name = itemView.findViewById<TextView>(R.id.item_poke_name)
            val lvl = itemView.findViewById<TextView>(R.id.item_poke_lvl)
            val hpBar = itemView.findViewById<ProgressBar>(R.id.item_poke_hp_bar)
            val hpText = itemView.findViewById<TextView>(R.id.item_poke_hp_text)
            val icon = itemView.findViewById<ImageView>(R.id.item_poke_icon)

            //remplir item
            name.text = pokemon.species.nom
            lvl.text = "Lv. ${pokemon.level}"
            val maxHp = pokemon.getMaxHp()
            hpBar.max = maxHp
            hpBar.progress = pokemon.currentHp
            hpText.text = "${pokemon.currentHp}/$maxHp"
            updateHpColor(hpBar, pokemon.currentHp, maxHp)
            Glide.with(this).load(DataManager.model.getFrontSprite(pokemon.species.num)).into(icon)

            //grise si mort
            if (pokemon.isKO) itemView.alpha = 0.5f
            if (pokemon == playerPokemon) name.text = "${pokemon.species.nom} (Actif)"

            itemView.setOnClickListener {
                if (onPokemonSelected != null) {
                    //Mode sélection: on renvoie le Pokémon cliqué
                    layoutMenuEquipe.visibility = View.GONE
                    btnCloseTeam.visibility = View.VISIBLE //réactivation de la croix
                    onPokemonSelected(pokemon)
                } else {
                    //mode combat classique
                    if (pokemon == playerPokemon) Toast.makeText(this, "Déjà au combat !", Toast.LENGTH_SHORT).show()
                    else if (pokemon.isKO) Toast.makeText(this, "Ce Pokémon est K.O.", Toast.LENGTH_SHORT).show()
                    else {
                        changerPokemon(pokemon, etaitKo)
                        layoutMenuEquipe.visibility = View.GONE
                    }
                }
            }
            teamListContainer.addView(itemView)
        }
    }

    private fun setupBattle() {
        if (Player.getEquipe().isEmpty()) { finish(); return }
        if (!::playerPokemon.isInitialized || playerPokemon.isKO) playerPokemon = Player.getPremierPokemon()

        val estUnBoss = vagueActuelle % 10 == 0
        if (estUnBoss) MusicManager.jouerPlaylistBoss(this)

        genererEnnemi()

        //tous les 10 niveaux on remet la zik et on full heal
        if (vagueActuelle > 1 && (vagueActuelle - 1) % 10 == 0) {
            MusicManager.jouerPlaylistBattle(this)
            soignerTout()
        }
        updateUI(animate = false)

        val targetScale = if (estUnBoss) 1.4f else 1f

        //anim le premier spawn
        if (vagueActuelle == 1) {
            BattleAnimator.animateEntry(imgPokePlayer, isPlayer = true)
            MusicManager.crierPokemon(playerPokemon) //cri

            BattleAnimator.animateEntry(imgPokeEnemy, isPlayer = false, scaleTarget = targetScale)
            Handler(Looper.getMainLooper()).postDelayed({
                if (estUnBoss) MusicManager.crierBoss() else MusicManager.crierPokemon(enemyPokemon) //cri
            }, 500)
        } else {
            //spawn normal des autres
            BattleAnimator.animateEntry(imgPokeEnemy, isPlayer = false, scaleTarget = targetScale)
            if (estUnBoss) MusicManager.crierBoss() else MusicManager.crierPokemon(enemyPokemon) //cri Boss
        }

        currentTurn = 1
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
        animateText("Un ${enemyPokemon.species.nom} sauvage apparaît !")
    }

    private fun executerTourDeJeu(playerAttack: Attack) {
        isTurnInProgress = true
        currentTurn++
        numVague.text = "Vague $vagueActuelle | Tour $currentTurn"

        //bot recup atk random
        val enemyAttack = enemyPokemon.attacks.random()
        val pSpeed = playerPokemon.battleVit
        val eSpeed = enemyPokemon.battleVit

        //check qui commence
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
                MusicManager.jouerSonAttaque(move1)

                fun executerImpact1() {
                    //frappe du joueur 1
                    val efficaciteMsg = applicationDegats(first, second, move1)
                    if (move1.basePower > 0) {
                        if (efficaciteMsg.contains("Coup critique !")) BattleAnimator.animateCritShake(viewSecond)
                        else BattleAnimator.animateHit(viewSecond)
                    }

                    fun suiteApresAttaque() {
                        if (second.isKO) {
                            BattleAnimator.animateKO(viewSecond, second) { finDuCombat(second) }
                        } else {
                            //tour joueur 2 si en vie
                            animateText("${second.species.nom} utilise ${move2.name} !") {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    MusicManager.jouerSonAttaque(move2)

                                    fun executerImpact2() {
                                        //frappe joueur 2
                                        val effMsg2 = applicationDegats(second, first, move2)
                                        if (move2.basePower > 0) {
                                            if (effMsg2.contains("Coup critique !")) BattleAnimator.animateCritShake(viewFirst)
                                            else BattleAnimator.animateHit(viewFirst)
                                        }

                                        fun finDuTour() {
                                            if (first.isKO) {
                                                BattleAnimator.animateKO(viewFirst, first) { finDuCombat(first) }
                                            } else {
                                                //heal passif genre restes
                                                appliquerEffetsFinDeTour(first) {
                                                    appliquerEffetsFinDeTour(second) {
                                                        isTurnInProgress = false
                                                        animateText("Que doit faire ${playerPokemon.species.nom} ?")
                                                    }
                                                }
                                            }
                                        }

                                        if (effMsg2.isNotEmpty()) {
                                            afficherDialoguesSuccessifs(effMsg2) { finDuTour() }
                                        } else {
                                            finDuTour()
                                        }
                                    }

                                    //anim atk joueur 2
                                    if (move2.basePower > 0) {
                                        BattleAnimator.animateAttackMove(viewSecond, !isPlayerFirst) { executerImpact2() }
                                    } else {
                                        Handler(Looper.getMainLooper()).postDelayed({ executerImpact2() }, 300)
                                    }
                                }, 200)
                            }
                        }
                    }

                    if (efficaciteMsg.isNotEmpty()) {
                        afficherDialoguesSuccessifs(efficaciteMsg) { suiteApresAttaque() }
                    } else {
                        suiteApresAttaque()
                    }
                }

                //anim atk joueur 1
                if (move1.basePower > 0) {
                    BattleAnimator.animateAttackMove(viewFirst, isPlayerFirst) { executerImpact1() }
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({ executerImpact1() }, 300)
                }
            }, 200)
        }
    }

    private fun tourEnnemiSeul() {
        if(playerPokemon.isKO) { isTurnInProgress = false; return }
        val enemyAttack = enemyPokemon.attacks.random()

        animateText("${enemyPokemon.species.nom} utilise ${enemyAttack.name} !") {
            Handler(Looper.getMainLooper()).postDelayed({
                MusicManager.jouerSonAttaque(enemyAttack)

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
                        afficherDialoguesSuccessifs(effMsg) { checkFinTour() }
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

        //si la bete a des restes, heal
        if (quantiteRestes > 0 && !pokemon.isKO && pokemon.currentHp < pokemon.getMaxHp()) {
            MusicManager.jouerSonBattle("item_active")
            pokemon.heal(((pokemon.getMaxHp() * quantiteRestes) / 16).coerceAtLeast(1))
            updateUI(true)
            Handler(Looper.getMainLooper()).postDelayed({ onTermine() }, 500)
        } else onTermine()
    }

    private fun soignerTout() {
        for (pokemon in Player.getEquipe()) {
            pokemon.heal(pokemon.getMaxHp())
            for (i in pokemon.attacks.indices) pokemon.currentPP[i] = pokemon.attacks[i].pp
        }
    }

    private fun genererEnnemi() {
        enemyPokemon = modelJson.getRandomPokemon()
        val estUnBoss = vagueActuelle % 10 == 0
        val baseLvl = vagueActuelle
        var randomLvl = (baseLvl - 1..baseLvl + 1).random().coerceAtLeast(1)

        //Boss très fort
        if (estUnBoss) {
            randomLvl += (5..8).random() //Gap de niveau bien plus grand
        } else {
            imgPokeEnemy.scaleX = 1f; imgPokeEnemy.scaleY = 1f
        }

        //monte le poké de level
        enemyPokemon.level = 1
        for (i in 1 until randomLvl) enemyPokemon.monterLevel()

        val objects = listOf("atk_plus", "def_plus", "vit_plus", "pv_plus", "item_restes", "item_bague_force", "item_veste_combat", "item_cape_vitesse")

        //Boss gagne 1 objet toutes les 2 ou 3 vagues, avec un minimum garanti
        val nbObj = if (estUnBoss) (vagueActuelle / 4).coerceAtLeast(1)..(vagueActuelle / 2).coerceAtLeast(2) else 0..(enemyPokemon.level / 5)

        //ajout objet random
        repeat(nbObj.random()) { enemyPokemon.ajouterObjet(objects.random()) }

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
                    if(etaitKo) { isTurnInProgress = false; animateText("Que doit faire ${playerPokemon.species.nom} ?") }
                    else tourEnnemiSeul()
                }, 1000)
            }
        }
    }

    fun ajouterOr(montant: Int) { nbPieceGagnee += montant }

    private fun resetPartieDonnees() {
        nbPieceGagnee = 0; vagueActuelle = 1; currentTurn = 1
        for (pokemon in Player.getEquipe()) {
            pokemon.species = pokemon.originalSpecies
            pokemon.objets.clear()
            pokemon.isKO = false; pokemon.level = 1; pokemon.exp = 0

            //reset des stages lors d'une nouvelle partie
            pokemon.resetStagesCombat()
            pokemon.recalculerStats()
            pokemon.currentHp = pokemon.getMaxHp()

            //reset des pp
            for (i in pokemon.attacks.indices) pokemon.currentPP[i] = pokemon.attacks[i].pp
        }
    }

    private fun applicationDegats(attaquant: Pokemon, defenseur: Pokemon, attaque: Attack): List<String> {
        val messagesRetour = mutableListOf<String>()
        //pp en moins
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
                attaquant.heal((attaquant.getMaxHp() * attaque.heal) / 100)
                updateUI(animate = true)
            }
            //Bonus/Malus
            messagesRetour.addAll(appliquerEffetsStats(attaquant, defenseur, attaque))

            //rien
            if (messagesRetour.isEmpty() && attaque.heal == 0) messagesRetour.add("Mais il ne se passe rien...")
            //on s'arrete là, pas besoin de calculer l'efficacité d'un buff ou heal
            return messagesRetour
        }

        //ATTAQUES AVEC DÉGÂTS

        //calcul puissance brute
        val levelFactor = (2 * attaquant.level / 5) + 2

        val statRatio = attaquant.battleAtk.toDouble() / defenseur.battleDef.toDouble()
        var degats = (((levelFactor * attaque.basePower * statRatio) / 50) + 2).toDouble()

        //safe si dmg tres bas
        if (degats < 1) degats = 1.0

        //gestion miss et esquive
        if (Random.nextDouble() >= attaque.accuracy) {
            messagesRetour.add("${defenseur.species.nom} évite l'attaque !")
            return messagesRetour //ahh il a raté le looser
        }

        //gestion critique
        val isCrit = Random.nextDouble() < attaque.critRatio
        if (isCrit) degats *= 1.5

        //calcul efficacité des types
        val typeAtkEnum = try { PokemonType.valueOf(attaque.type.uppercase()) } catch (e: Exception) { PokemonType.NORMAL }
        val multiplicateur = PokemonType.calculerEfficaciteContre(typeAtkEnum, defenseur)

        //msg diff fonction des types
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
        var degatsFinal = (degats * multiplicateur * stabMultiplicateur).toInt()

        //save si attaque est très basse
        if (degatsFinal < 1) degatsFinal = 1

        defenseur.prendreDmg(degatsFinal)

        //Bruitage Alarme PV Bas
        if (defenseur == playerPokemon && !defenseur.isKO && (defenseur.currentHp.toFloat() / defenseur.getMaxHp()) <= 0.2f) MusicManager.jouerSonBattle("low_hp")

        //Vol de vie genre giga sangsue
        if (attaque.drain && degatsFinal > 0) {
            val drain = degatsFinal / 2
            if (drain > 0) { MusicManager.jouerSonBattle("heal"); attaquant.heal(drain) }
        }

        //bonus_malus associés à l'attaque
        messagesRetour.addAll(appliquerEffetsStats(attaquant, defenseur, attaque))
        updateUI(animate = true)
        return messagesRetour
    }

    private fun appliquerEffetsStats(attaquant: Pokemon, defenseur: Pokemon, attaque: Attack): List<String> {
        val messages = mutableListOf<String>()
        var atkBuff = false; var atkDebuff = false; var defBuff = false; var defDebuff = false

        //bonus
        attaque.bonus?.forEach { map ->
            map.forEach { (stat, valeur) ->
                val msg = appliquerStatChange(attaquant, stat, valeur)
                if (msg.isNotEmpty()) { messages.add(msg); if (valeur > 0) atkBuff = true else atkDebuff = true }
            }
        }

        //malus
        attaque.malus?.forEach { map ->
            map.forEach { (stat, valeur) ->
                val msg = appliquerStatChange(defenseur, stat, valeur)
                if (msg.isNotEmpty()) { messages.add(msg); if (valeur > 0) defBuff = true else defDebuff = true }
            }
        }

        //anim attaquant stat up/down
        if (atkBuff) { MusicManager.jouerSonBattle("bonus_stat_sound"); BattleAnimator.animateBuff(if (attaquant == playerPokemon) imgPokePlayer else imgPokeEnemy) }
        else if (atkDebuff) { MusicManager.jouerSonBattle("malus_stat_sound"); BattleAnimator.animateDebuff(if (attaquant == playerPokemon) imgPokePlayer else imgPokeEnemy) }

        //anim defenseur stat up/down
        if (defDebuff) { MusicManager.jouerSonBattle("malus_stat_sound"); BattleAnimator.animateDebuff(if (defenseur == playerPokemon) imgPokePlayer else imgPokeEnemy) }
        else if (defBuff) { MusicManager.jouerSonBattle("bonus_stat_sound"); BattleAnimator.animateBuff(if (defenseur == playerPokemon) imgPokePlayer else imgPokeEnemy) }

        return messages
    }

    private fun appliquerStatChange(cible: Pokemon, stat: String, niveau: Int): String {
        // Appelle la nouvelle mécanique de paliers définie dans Pokemon.kt
        return cible.modifierStage(stat, niveau)
    }

    private fun finDuCombat(loser: Pokemon) {
        val estUnBoss = vagueActuelle % 10 == 0

        if (loser == enemyPokemon) {
            val listeDialogues = ArrayList<String>()
            listeDialogues.add("${enemyPokemon.species.nom} est K.O.")

            //SOUND EFFECT DU BOSS BATTU + RECOMPENSES BONUS
            if (estUnBoss) {
                MusicManager.jouerSonBattle("victory_boss")
                ajouterOr(100)
                listeDialogues.add("Vous obtenez 100 or pour cette victoire !")
            }

            //formule pour exp
            var xpGain = 20 + (enemyPokemon.currentAtk + enemyPokemon.currentDef + enemyPokemon.currentVit + enemyPokemon.getMaxHp())/5 + (enemyPokemon.level*20)
            if (estUnBoss) xpGain *= 2 //x2 XP pour un boss

            for(poke in Player.getEquipe()){
                if(!poke.isKO){
                    val gainReel = if (poke == playerPokemon) xpGain else xpGain/2
                    val aLevelUp = poke.gagnerExperience(gainReel)

                    var msgXP = "${poke.species.nom} gagne $gainReel XP"
                    msgXP += if (aLevelUp) ", il passe au niveau ${poke.level}." else "."
                    listeDialogues.add(msgXP)

                    //on vérif si évo
                    if (aLevelUp && poke.species.evoLevel != null && poke.species.evoLevel!! <= poke.level){
                        listeDialogues.add("Hein ? ${poke.species.nom} évolue !")

                        val oldName = poke.species.nom
                        val oldLevel = poke.level
                        poke.species = modelJson.creerPokemon(poke.species.evo).species
                        poke.level = 1

                        //rattrape les level du pke
                        for (i in 1 until oldLevel) poke.monterLevel()
                        poke.currentHp = poke.getMaxHp()

                        listeDialogues.add("$oldName a évolué en ${poke.species.nom} !")
                    }
                }
            }

            afficherDialoguesSuccessifs(listeDialogues) {
                if (!isDestroyed && !isFinishing) {
                    RewardBattleVague(this, playerPokemon) { messagesReward ->
                        afficherDialoguesSuccessifs(messagesReward) {
                            //reset stats après vague
                            playerPokemon.resetStagesCombat()
                            vagueActuelle++
                            currentTurn = 1
                            numVague.text = "Vague $vagueActuelle | Tour $currentTurn"
                            updateUI(animate = false)
                            setupBattle()
                            isTurnInProgress = false
                        }
                    }.show()
                }
            }
        } else {
            //si le player perd
            animateText("${playerPokemon.species.nom} est K.O...") {
                isTurnInProgress = false

                //check team entiere
                if (Player.getEquipe().all { it.isKO }) {
                    animateText("GAME OVER - Butin : $nbPieceGagnee Or") {
                        Handler(Looper.getMainLooper()).postDelayed({ quitterPartie() }, 800)
                    }
                } else {
                    afficherMenuEquipe(true)
                }
            }
        }
    }

    private fun afficherDialoguesSuccessifs(messages: List<String>, onFinished: () -> Unit) {
        if (messages.isEmpty()) { onFinished(); return }
        val messageActuel = messages[0]
        val isXpMsg = messageActuel.contains("XP") || messageActuel.contains("niveau") || messageActuel.contains("gagne")
        val vitesse = if (isXpMsg) VIT_TEXTE_XP else VIT_TEXTE_NORMAL

        //son pour event special
        if (messageActuel.contains("passe au niveau")) MusicManager.sonLevelUpPoke()
        else if (messageActuel.contains("évolué")) MusicManager.sonEvoPoke()

        animateText(messageActuel, vitesse) {
            afficherDialoguesSuccessifs(messages.drop(1), onFinished)
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
                    //délai de lecture intégré à la fin de chaque phrase
                    textAnimHandler.postDelayed({
                        isTextWriting = false
                        currentTextRunnable = null
                        onCompleted?.invoke()
                    }, PAUSE_LECTURE * 2)
                }
            }
        }
        textAnimHandler.post(currentTextRunnable!!)
    }

    //UI
    private fun updateUI(animate: Boolean) {
        if(isDestroyed || isFinishing) return

        //update hp du joueur
        txtPlayerName.text = playerPokemon.species.nom
        txtPlayerLvl.text = "Lv${playerPokemon.level}"
        val maxP = playerPokemon.getMaxHp()
        if (animate) animateHpChange(playerHpBar, txtPlayerHpText, playerPokemon.currentHp, maxP)
        else {
            playerHpBar.max = maxP; playerHpBar.progress = playerPokemon.currentHp
            txtPlayerHpText.text = "${playerPokemon.currentHp} / $maxP"
            updateHpColor(playerHpBar, playerPokemon.currentHp, maxP)
        }
        Glide.with(this).load(DataManager.model.getBackSprite(playerPokemon.species.num)).into(imgPokePlayer)

        //update hp de l'ennemi
        txtEnemyName.text = enemyPokemon.species.nom
        txtEnemyLvl.text = "Lv${enemyPokemon.level}"
        val maxE = enemyPokemon.getMaxHp()
        if (animate) animateHpChange(enemyHpBar, null, enemyPokemon.currentHp, maxE)
        else {
            enemyHpBar.max = maxE; enemyHpBar.progress = enemyPokemon.currentHp
            updateHpColor(enemyHpBar, enemyPokemon.currentHp, maxE)
        }
        Glide.with(this).load(DataManager.model.getFrontSprite(enemyPokemon.species.num)).into(imgPokeEnemy)

        //MAJ des objets équipés sur le terrain
        afficherObjets(playerPokemon, playerItemsContainer)
        afficherObjets(enemyPokemon, enemyItemsContainer)
    }

    fun afficherObjets(pokemon: Pokemon, container: LinearLayout) {
        container.removeAllViews()
        pokemon.objets.forEach { (id, quantite) ->
            if (quantite > 0) {
                //layout de base de l'icone d'objet
                val frame = FrameLayout(this)
                val size = (24 * resources.displayMetrics.density).toInt()
                val frameParams = LinearLayout.LayoutParams(size, size)
                frameParams.setMargins(0, 0, (4 * resources.displayMetrics.density).toInt(), 0)
                frame.layoutParams = frameParams

                //l'image de l'objet
                val img = ImageView(this)
                img.layoutParams = FrameLayout.LayoutParams(-1, -1)
                img.setImageResource(UIHelper.getIconForObjet(id))
                frame.addView(img)

                //si on a + de 1 objet du meme type, met le "x2"
                if (quantite > 1) {
                    val txt = TextView(this)
                    txt.layoutParams = FrameLayout.LayoutParams(-2, -2).apply { gravity = Gravity.BOTTOM or Gravity.END }
                    txt.text = "x$quantite"; txt.textSize = 10f; txt.setTextColor(Color.WHITE)
                    txt.setShadowLayer(3f, 1f, 1f, Color.BLACK);
                    txt.setTypeface(null, Typeface.BOLD)
                    frame.addView(txt)
                }
                container.addView(frame)
            }
        }
    }

    private fun animateHpChange(bar: ProgressBar, txt: TextView?, target: Int, max: Int) {
        if(isDestroyed || isFinishing) return
        bar.max = max

        //safe check de fin
        if (bar.progress == target) { updateHpColor(bar, target, max); txt?.text = "$target / $max"; return }

        //animation de la barre de pv
        ObjectAnimator.ofInt(bar, "progress", bar.progress, target).apply {
            duration = 600; interpolator = FastOutSlowInInterpolator()
            addUpdateListener { updateHpColor(bar, it.animatedValue as Int, max);
                txt?.text = "${it.animatedValue as Int} / $max"
            }
            start()
        }
    }

    private fun updateHpColor(bar: ProgressBar, current: Int, max: Int) {
        val pct = (current.toFloat() / max.toFloat()) * 100
        bar.progressDrawable = getDrawable(when {
            pct > 50 -> R.drawable.hp_bar_green;
            pct > 20 -> R.drawable.hp_bar_yellow;
            else -> R.drawable.hp_bar_red
        })
    }

    private fun setupFullscreen() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}