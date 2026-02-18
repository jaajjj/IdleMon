package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide

class TenPullActivity : BaseActivity(), PanoramaUI {

    private lateinit var capteurManager: CapteurManager

    override lateinit var backgroundImage: ImageView
    override lateinit var eggsContainer: FrameLayout
    override lateinit var boussole: ImageView

    override val context: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_pull)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        backgroundImage = findViewById(R.id.background360)
        eggsContainer = findViewById(R.id.eggsContainer)
        boussole = findViewById(R.id.boussole)
        val catchBtn = findViewById<Button>(R.id.catchBtn)

        capteurManager = CapteurManager(this, eggCount = 7, isTenPull = true)

        lifecycleScope.launch {
            backgroundImage.post {
                lifecycleScope.launch {
                    capteurManager.loadEggsAsync()
                }
            }
        }

        boussole.setOnClickListener {
            capteurManager.toggleMode()
        }
        catchBtn.setOnClickListener {
            val selected = capteurManager.selectedEgg
            if (selected != null) {
                val pokemonsList = selected.tag as? List<Pokemon>

                if (pokemonsList != null && pokemonsList.isNotEmpty()) {
                    val resultList = mutableListOf<Pair<Pokemon, Boolean>>()

                    for(pokemon in pokemonsList) {
                        val estDejaPossede = Player.aDejaLePokemon(pokemon.species.num)

                        if(estDejaPossede){
                            when{
                                pokemon.species.rarete == "Legendaire" -> Player.addPieces(500)
                                pokemon.species.rarete == "Fabuleux" -> Player.addPieces(200)
                                pokemon.species.rarete == "Epique" -> Player.addPieces(100)
                                else -> Player.addPieces(50)
                            }
                        } else {
                            Player.getBoxPokemon().add(pokemon)
                        }
                        resultList.add(Pair(pokemon, estDejaPossede))
                    }

                    capteurManager.stop()
                    capteurManager.cleanUpResources()
                    showResultDialog(resultList)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) capteurManager.handleTouch(event)
        return super.onTouchEvent(event)
    }
    private fun showResultDialog(results: List<Pair<Pokemon, Boolean>>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.result_ten_pull)
        dialog.setCancelable(false)
        val container = dialog.findViewById<LinearLayout>(R.id.containerTenPokemons)
        val btnQuit = dialog.findViewById<Button>(R.id.quitTenPullBtn)
        val inflater = LayoutInflater.from(this)

        for ((pokemon, isDuplicate) in results) {
            val itemView = inflater.inflate(R.layout.item_ten_pull, container, false)

            val txtNom = itemView.findViewById<TextView>(R.id.nomPok)
            val txtRarete = itemView.findViewById<TextView>(R.id.raretePok)
            val imgPoke = itemView.findViewById<ImageView>(R.id.imgPokeSmall)
            val imgSoldOut = itemView.findViewById<ImageView>(R.id.imgSoldOutSmall)

            if (isDuplicate) {
                imgSoldOut.visibility = View.VISIBLE
                imgPoke.alpha = 0.5f
            } else {
                imgSoldOut.visibility = View.GONE
            }

            val rareteColor = when (pokemon.species.rarete) {
                "Legendaire" -> Color.parseColor("#2196F3")
                "Fabuleux" -> Color.parseColor("#4CAF50")
                "Epique" -> Color.parseColor("#9C27B0")
                "Rare" -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#000000")
            }
            txtNom.text = pokemon.species.nom
            txtNom.setTextColor(rareteColor)
            txtRarete.text = pokemon.species.rarete
            txtRarete.setTextColor(rareteColor)

            Glide.with(this).asGif().load(DataManager.model.getFrontSprite(pokemon.species.num)).into(imgPoke)
            container.addView(itemView)
        }

        btnQuit.setOnClickListener {
            MusicManager.jouerPlaylistHome(this)
            dialog.dismiss()
            finish()
        }
        dialog.show()
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onResume() { super.onResume(); capteurManager.start() }
    override fun onPause() { super.onPause(); capteurManager.stop() }
}