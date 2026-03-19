package com.example.idlemon

import android.app.Dialog
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PokedexActivity : BaseActivity() {

    // UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var searchBar: EditText
    private lateinit var txtCounter: TextView
    private lateinit var spinnerType: Spinner
    private lateinit var chkOwned: CheckBox
    private lateinit var adapter: PokedexAdapter

    // Données
    private var toutesLesEspeces: List<PokemonSpecies> = listOf()
    private var especesFiltrees: List<PokemonSpecies> = listOf()
    private val idsPossedes = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokedex)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        initViews()
        chargerDonnees()
        setupFiltres()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        txtCounter = findViewById(R.id.txtCounter)
        searchBar = findViewById(R.id.searchBar)
        spinnerType = findViewById(R.id.spinnerType)
        chkOwned = findViewById(R.id.chkOwned)
        recyclerView = findViewById(R.id.pokedexRecyclerView)

        // 5 colonnes
        recyclerView.layoutManager = GridLayoutManager(this, 5)
    }

    private fun chargerDonnees() {
        // 1. IDs possédés
        idsPossedes.clear()
        Player.getEquipe().forEach { idsPossedes.add(it.species.num) }
        Player.getBoxPokemon().forEach { idsPossedes.add(it.species.num) }

        // 2. Toutes les espèces
        toutesLesEspeces = DataManager.model.getAllPokemonSpecies().sortedBy { it.num }
        especesFiltrees = toutesLesEspeces

        // 3. Mise à jour du compteur global (X possédés sur Y total)
        txtCounter.text = "${idsPossedes.size} / ${toutesLesEspeces.size} capturés"

        // 4. Initialisation de l'Adapter
        adapter = PokedexAdapter(especesFiltrees, idsPossedes) { species, estPossede ->
            afficherPopupPokemon(species, estPossede)
        }
        recyclerView.adapter = adapter
    }

    private fun setupFiltres() {
        // --- Setup du Spinner (Liste des types) ---
        val types = listOf(
            "Tous les types", "Acier", "Combat", "Dragon", "Eau", "Electrik",
            "Fee", "Feu", "Glace", "Insecte", "Normal", "Plante", "Poison",
            "Psy", "Roche", "Sol", "Spectre", "Tenebre", "Vol"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        spinnerType.adapter = spinnerAdapter

        // --- Listeners ---

        // Listener pour la recherche de texte
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { appliquerFiltres() }
        })

        // Listener pour le dropdown de type
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { appliquerFiltres() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Listener pour la case à cocher "Possédés"
        chkOwned.setOnCheckedChangeListener { _, _ -> appliquerFiltres() }
    }

    // Le cœur du système : combine tous les filtres en même temps !
    private fun appliquerFiltres() {
        val texte = searchBar.text.toString().trim().lowercase()
        val typeSelectionne = spinnerType.selectedItem.toString()
        val seulementPossedes = chkOwned.isChecked

        especesFiltrees = toutesLesEspeces.filter { species ->

            // 1. Filtre Texte
            val correspondTexte = texte.isEmpty() ||
                    species.nom.lowercase().contains(texte) ||
                    species.num.toString() == texte

            // 2. Filtre Type
            val correspondType = typeSelectionne == "Tous les types" ||
                    species.type.any { it.nom.equals(typeSelectionne, ignoreCase = true) }

            // 3. Filtre Possédé
            val correspondPossede = !seulementPossedes || idsPossedes.contains(species.num)

            // On garde le Pokémon SEULEMENT s'il passe les 3 tests
            correspondTexte && correspondType && correspondPossede
        }

        // On envoie la liste filtrée à l'adapter
        adapter.updateData(especesFiltrees)
    }

    private fun afficherPopupPokemon(species: PokemonSpecies, estPossede: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_pokedex_info)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtNum = dialog.findViewById<TextView>(R.id.dialogNum)
        val txtRarete = dialog.findViewById<TextView>(R.id.dialogRarete)
        val imgPoke = dialog.findViewById<ImageView>(R.id.dialogImage)
        val txtName = dialog.findViewById<TextView>(R.id.dialogName)
        val type1 = dialog.findViewById<ImageView>(R.id.dialogType1)
        val type2 = dialog.findViewById<ImageView>(R.id.dialogType2)
        val txtPrevo = dialog.findViewById<TextView>(R.id.dialogPrevo)
        val txtEvo = dialog.findViewById<TextView>(R.id.dialogEvo)
        val txtDesc = dialog.findViewById<TextView>(R.id.dialogDesc)
        val separator = dialog.findViewById<View>(R.id.dialogSeparator)
        val evoTitle = dialog.findViewById<TextView>(R.id.dialogEvoTitle)

        // Affichage du numéro garanti pour tout le monde
        txtNum.text = "#${species.num.toString().padStart(3, '0')}"

        if (estPossede) {
            // --- POKÉMON DÉCOUVERT ---
            txtName.text = species.nom
            txtDesc.text = "✔️ Enregistré dans le Pokédex"

            // Rareté avec couleur
            txtRarete.text = species.rarete
            txtRarete.setTextColor(getColorForRarity(species.rarete))
            txtRarete.visibility = View.VISIBLE

            // Types
            type1.setImageResource(DataManager.model.getIconType(species.type[0].nom))
            type1.visibility = View.VISIBLE
            if (species.type.size > 1) {
                type2.setImageResource(DataManager.model.getIconType(species.type[1].nom))
                type2.visibility = View.VISIBLE
            } else {
                type2.visibility = View.GONE
            }

            // --- CHRONOLOGIE D'ÉVOLUTION ---
            separator.visibility = View.VISIBLE
            evoTitle.visibility = View.VISIBLE

            // Pré-évolution
            if (species.prevo != null) {
                txtPrevo.text = "← Évolue depuis : ${species.prevo}"
                txtPrevo.visibility = View.VISIBLE
            } else {
                txtPrevo.visibility = View.GONE
            }

            // Évolution future
            if (species.evo != null) {
                val nivText = if (species.evoLevel != null) " (Niv. ${species.evoLevel})" else ""
                txtEvo.text = "→ Évolue en : ${species.evo}$nivText"
                txtEvo.visibility = View.VISIBLE
            } else {
                txtEvo.visibility = View.GONE
            }

            // S'il n'a ni pré-évolution ni évolution (Ex: Tauros, Légendaires)
            if (species.prevo == null && species.evo == null) {
                txtEvo.text = "Stade unique (Pas d'évolution)"
                txtEvo.visibility = View.VISIBLE
            }

            // Image HD Couleur
            imgPoke.clearColorFilter()
            imgPoke.alpha = 1.0f
            Glide.with(this).load(DataManager.model.getFrontSprite(species.num)).into(imgPoke)

        } else {
            // --- POKÉMON INCONNU ---
            txtName.text = "???"
            txtDesc.text = "❌ Espèce inconnue. Continuez de jouer pour l'obtenir !"

            txtRarete.visibility = View.GONE
            type1.visibility = View.GONE
            type2.visibility = View.GONE
            txtPrevo.visibility = View.GONE
            txtEvo.visibility = View.GONE
            separator.visibility = View.GONE
            evoTitle.visibility = View.GONE

            // Effet Grisé + Transparent
            val matrix = ColorMatrix().apply { setSaturation(0f) }
            imgPoke.colorFilter = ColorMatrixColorFilter(matrix)
            imgPoke.alpha = 0.5f

            Glide.with(this).load(DataManager.model.getFrontSprite(species.num)).into(imgPoke)
        }

        dialog.show()
    }
    private fun getColorForRarity(rarete: String): Int {
        return when (rarete.lowercase()) {
            "commun" -> Color.parseColor("#A0A0A0")     // Gris
            "peu commun" -> Color.parseColor("#4CAF50") // Vert
            "rare" -> Color.parseColor("#2196F3")       // Bleu
            "epique", "épique" -> Color.parseColor("#9C27B0") // Violet
            "fabuleux" -> Color.parseColor("#E91E63")   // Rose/Rouge
            "legendaire", "légendaire" -> Color.parseColor("#FFC107") // Doré
            else -> Color.parseColor("#333333") // Sombre par défaut
        }
    }
}