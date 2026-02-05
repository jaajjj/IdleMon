package com.example.idlemon

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class SinglePullActivity : AppCompatActivity() {

    //UI
    private lateinit var capteurManager: CapteurManager
    lateinit var backgroundImage: ImageView
    lateinit var eggsContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_pull)

        //sans barre sys
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //background et egg init
        backgroundImage = findViewById(R.id.background360)
        eggsContainer = findViewById(R.id.eggsContainer)
        val catchBtn = findViewById<Button>(R.id.catchBtn)

        //capteur
        capteurManager = CapteurManager(this)

        catchBtn.setOnClickListener {
            val selected = capteurManager.selectedEgg
            if (selected != null) {
                val pokemon = selected.tag as Pokemon
                Player.addPokemon(pokemon)
                capteurManager.stop() // On fige le fond
                showResultDialog(pokemon)
            }
        }
    }

    private fun showResultDialog(pokemon: Pokemon) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.result_single_pull)
        dialog.setCancelable(false) // Empêche l'interaction avec le fond

        val txtNom = dialog.findViewById<TextView>(R.id.pokemonNomPull)
        val txtRarete = dialog.findViewById<TextView>(R.id.raretePull)
        val imgPoke = dialog.findViewById<ImageView>(R.id.imgPokemonPull)
        val btnQuit = dialog.findViewById<Button>(R.id.quitPullBtn)

        // Gestion des couleurs selon la rareté
        val rareteColor = when (pokemon.species.rarete) {
            "Légendaire", "Legendaire" -> Color.parseColor("#2196F3") // Bleu
            "Fabuleux" -> Color.parseColor("#4CAF50")                // Vert
            "Épique", "Epique" -> Color.parseColor("#9C27B0")        // Violet
            "Rare" -> Color.parseColor("#FF9800")                   // Orange
            else -> Color.parseColor("#000000")                     // Noir (Commun/Peu commun)
        }

        txtNom.text = pokemon.species.nom
        txtNom.setTextColor(rareteColor)

        txtRarete.text = pokemon.species.rarete
        txtRarete.setTextColor(rareteColor)

        Glide.with(this)
            .asGif()
            .load(DataManager.model.getFrontSprite(pokemon.species.num))
            .into(imgPoke)

        // Action du bouton Récupérer
        btnQuit.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onResume() {
        super.onResume()
        capteurManager.start()
    }

    override fun onPause() {
        super.onPause()
        capteurManager.stop()
    }
}

/**
 * GESTIONNAIRE DE CAPTEURS ET RENDU 360
 */

class CapteurManager(private val activity: SinglePullActivity) : SensorEventListener {

    //sensor et panorama
    private val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val compas = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    private var panoramaShader: BitmapShader? = null
    private var shaderPaint: Paint? = null
    private val view360 = Matrix()

    private var imgWidth = 0f
    private var currentDegree = 0f
    private val valFluide = 0.4f

    //stock l'oeuf selec
    var selectedEgg: View? = null

    init {
        //config de la vue en 360
        activity.backgroundImage.post {
            setupPanorama()
            generateEggs()
        }
    }

    private fun setupPanorama() {
        val bitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.back_pull) ?: return
        val scale = activity.backgroundImage.height.toFloat() / bitmap.height.toFloat()
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, activity.backgroundImage.height, true)

        //taille img panorama et application de la "tapisserie"
        imgWidth = scaledWidth.toFloat()
        panoramaShader = BitmapShader(scaledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        shaderPaint = Paint().apply {
            shader = panoramaShader
            isAntiAlias = true
        }

        activity.backgroundImage.setImageDrawable(object : Drawable() {
            override fun draw(canvas: Canvas) {
                shaderPaint?.let { canvas.drawPaint(it) }
            }
            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        })
    }

    private fun generateEggs() {
        activity.eggsContainer.removeAllViews()
        val pokemonsGacha = List(5) { DataManager.model.getRandomPokemon() }
        val inflater = LayoutInflater.from(activity)

        for (set in -1..1) {
            val offsetSet = set * imgWidth
            for (i in 0 until 5) {
                val eggLayout = inflater.inflate(R.layout.classic_egg, activity.eggsContainer, false)
                val pokePull = pokemonsGacha[i]
                val eggImage = eggLayout.findViewById<ImageView>(R.id.imageView5)

                // Choix de l'oeuf selon rareté
                val eggDrawable = when(pokePull.species.rarete) {
                    "Legendaire" -> R.drawable.egg_leg
                    "Fabuleux" -> R.drawable.egg_fab
                    "Epique" -> R.drawable.egg_epique
                    else -> R.drawable.egg
                }

                Glide.with(activity)
                    .asGif()
                    .load(eggDrawable)
                    .override(500, 500)
                    .centerCrop()
                    .into(eggImage)

                eggLayout.tag = pokePull
                val size = 600
                eggLayout.layoutParams = FrameLayout.LayoutParams(size, size)
                eggLayout.x = (i.toFloat() / 5f * imgWidth) + offsetSet
                eggLayout.y = (activity.backgroundImage.height / 2f) - (size / 2f)

                activity.eggsContainer.addView(eggLayout)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION && imgWidth > 0) {

            //calculs rotations
            val targetDegree = event.values[0]
            var diff = targetDegree - currentDegree
            if (diff > 180) diff -= 360
            if (diff < -180) diff += 360

            currentDegree += diff * valFluide
            if (currentDegree >= 360) currentDegree -= 360
            if (currentDegree < 0) currentDegree += 360

            val fraction = currentDegree / 360f
            val scrollX = fraction * imgWidth

            view360.setTranslate(-scrollX, 0f)
            panoramaShader?.setLocalMatrix(view360)
            activity.backgroundImage.invalidate()

            val screenWidth = activity.backgroundImage.width.toFloat()
            val screenCenter = screenWidth / 2f
            val containerTranslationX = -scrollX + screenCenter - 300f

            activity.eggsContainer.translationX = containerTranslationX

            //cherche gagnant
            var minDistance = Float.MAX_VALUE
            var winner: View? = null

            for (i in 0 until activity.eggsContainer.childCount) {
                val v = activity.eggsContainer.getChildAt(i)
                val eggCenterOnScreen = v.x + containerTranslationX + 300f

                // On ne cherche le winner que parmi les oeufs visibles
                if (eggCenterOnScreen > -500f && eggCenterOnScreen < screenWidth + 500f) {
                    //calcul + proche (kppv prime)
                    val dist = Math.abs(eggCenterOnScreen - screenCenter)
                    if (dist < minDistance) {
                        minDistance = dist
                        winner = v
                    }
                }
            }

            //effets et opti
            for (i in 0 until activity.eggsContainer.childCount) {
                val v = activity.eggsContainer.getChildAt(i)
                val eggCenterOnScreen = v.x + containerTranslationX + 300f
                val eggImage = v.findViewById<ImageView>(R.id.imageView5)
                val animatable = eggImage.drawable as? Animatable

                //opti si en dehors de l'écran
                if (eggCenterOnScreen < -600f || eggCenterOnScreen > screenWidth + 600f) {
                    v.visibility = View.GONE
                    continue
                }
                v.visibility = View.VISIBLE

                //effet du selec
                if (v == winner && minDistance < 450f) {
                    v.scaleX = 1.3f
                    v.scaleY = 1.3f
                    v.alpha = 1.0f
                    animatable?.start()
                } else {
                    v.scaleX = 0.8f
                    v.scaleY = 0.8f
                    v.alpha = 0.5f
                    animatable?.stop()
                }
            }
            selectedEgg = if (minDistance < 450f) winner else null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun start() = sensorManager.registerListener(this, compas, SensorManager.SENSOR_DELAY_UI)

    fun stop() = sensorManager.unregisterListener(this)
}