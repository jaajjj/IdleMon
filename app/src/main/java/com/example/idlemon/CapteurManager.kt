package com.example.idlemon

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers // Important
import kotlinx.coroutines.withContext // Important
import kotlin.math.abs

class CapteurManager(
    private val ui: PanoramaUI,
    private val eggCount: Int = 5,
    private val isTenPull: Boolean = false
) : SensorEventListener {

    private val sensorManager = ui.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val compas = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    private var panoramaShader: BitmapShader? = null
    private var shaderPaint: Paint? = null
    private val view360 = Matrix()
    private var imgWidth = 0f
    private var currentDegree = 0f
    private val valFluide = 0.4f
    var selectedEgg: View? = null
    private var isSensorMode = true
    private var lastTouchX = 0f
    private var flingVelocity = 0f
    private val friction = 0.7f
    private val sensiScroll = 0.2f

    private val flingRunnable = object : Runnable {
        override fun run() {
            if (abs(flingVelocity) < 0.5f) return
            currentDegree -= flingVelocity * sensiScroll
            flingVelocity *= friction
            renderPanorama()
            ui.backgroundImage.postDelayed(this, 16)
        }
    }

    init {
        // On initialise le fond (rapide)
        ui.backgroundImage.post {
            setupPanorama()
        }
        // NOTE : On ne génère plus les œufs ici, on attend l'appel de loadEggsAsync()
    }

    /**
     * Fonction suspendue : Calcule les Pokémon en arrière-plan (IO)
     * puis affiche les oeufs sur le thread principal (Main).
     */
    suspend fun loadEggsAsync() {
        //Charge les oeufs dans le thread IO
        val preparedEggs = withContext(Dispatchers.IO) {
            val list = mutableListOf<Pair<List<Pokemon>, Int>>()

            for (i in 0 until eggCount) {
                // Génération aléatoire (peut être lent)
                val pokemonsInEgg = if (isTenPull) {
                    List(10) { DataManager.model.getRandomPokemon() }
                } else {
                    listOf(DataManager.model.getRandomPokemon())
                }

                //rareté max pour les oeufs
                val bestPokemon = pokemonsInEgg.maxByOrNull { getRarityScore(it.species.rarete) }
                    ?: pokemonsInEgg.first()

                val drawableRes = when(bestPokemon.species.rarete) {
                    "Legendaire" -> R.drawable.egg_leg
                    "Fabuleux" -> R.drawable.egg_fab
                    "Epique" -> R.drawable.egg_epique
                    else -> R.drawable.egg
                }

                list.add(Pair(pokemonsInEgg, drawableRes))
            }
            list //retourne la liste préparée
        }

        //création des vues sur le thread Main
        withContext(Dispatchers.Main) {
            displayEggs(preparedEggs)
        }
    }

    private fun displayEggs(preparedEggs: List<Pair<List<Pokemon>, Int>>) {
        if (imgWidth <= 0) return //le panorama n'est pas pret

        ui.eggsContainer.removeAllViews()
        val inflater = LayoutInflater.from(ui.context)

        for (set in -1..1) {
            val offsetSet = set * imgWidth
            for (i in 0 until preparedEggs.size) {
                val (pokemonsInEgg, drawableRes) = preparedEggs[i]
                //on gonfle en copiant classic_egg xml
                val eggLayout = inflater.inflate(R.layout.classic_egg, ui.eggsContainer, false)
                val eggImage = eggLayout.findViewById<ImageView>(R.id.imageView5)

                Glide.with(ui.context)
                    .load(drawableRes)
                    .override(500, 500)
                    .centerCrop()
                    .skipMemoryCache(true) // Ignore le cache mémoire
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Ignore le cache disque
                    .error(android.R.drawable.ic_delete) // Affiche une croix rouge si l'image plante
                    .into(eggImage)

                eggLayout.tag = pokemonsInEgg
                val size = 600
                eggLayout.layoutParams = FrameLayout.LayoutParams(size, size)
                eggLayout.x = (i.toFloat() / eggCount.toFloat() * imgWidth) + offsetSet
                eggLayout.y = (ui.backgroundImage.height / 2f) - (size / 2f)
                ui.eggsContainer.addView(eggLayout)
            }
        }
    }

    private fun getRarityScore(rarete: String): Int {
        return when (rarete) {
            "Legendaire" -> 5
            "Fabuleux" -> 4
            "Epique" -> 3
            "Rare" -> 2
            else -> 1
        }
    }

    //sensor compas
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION && isSensorMode) {
            val targetDegree = event.values[0]
            var diff = targetDegree - currentDegree
            //modulo 360°
            if (diff > 180) diff -= 360
            if (diff < -180) diff += 360
            currentDegree += diff * valFluide
            renderPanorama()
        }
    }

    private fun renderPanorama() {
        if (imgWidth <= 0) return //panorama pas prêt, on attend
        if (currentDegree >= 360) currentDegree %= 360
        if (currentDegree < 0) currentDegree = 360 + (currentDegree % 360)

        val fraction = currentDegree / 360f //On prends une val entre 0 et 1
        val scrollX = fraction * imgWidth
        view360.setTranslate(-scrollX, 0f)
        panoramaShader?.setLocalMatrix(view360) //utilise matrix pour le rendu panorama 360
        ui.backgroundImage.invalidate()

        val screenWidth = ui.backgroundImage.width.toFloat()
        val screenCenter = screenWidth / 2f
        val containerTranslationX = -scrollX + screenCenter - 300f
        ui.eggsContainer.translationX = containerTranslationX

        var minDistance = Float.MAX_VALUE
        var winner: View? = null

        //calcul du plus proche oeuf (kppv prime)
        for (i in 0 until ui.eggsContainer.childCount) {
            val v = ui.eggsContainer.getChildAt(i)
            val eggCenterOnScreen = v.x + containerTranslationX + 300f
            if (eggCenterOnScreen > -500f && eggCenterOnScreen < screenWidth + 500f) {
                val dist = Math.abs(eggCenterOnScreen - screenCenter)
                if (dist < minDistance) {
                    minDistance = dist
                    winner = v
                }
            }
        }

        for (i in 0 until ui.eggsContainer.childCount) {
            val v = ui.eggsContainer.getChildAt(i)
            val eggCenterOnScreen = v.x + containerTranslationX + 300f
            val eggImage = v.findViewById<ImageView>(R.id.imageView5)
            val animatable = eggImage.drawable as? Animatable

            //on montre l'oeuf si il est à l'écran
            if (eggCenterOnScreen < -600f || eggCenterOnScreen > screenWidth + 600f) {
                v.visibility = View.GONE
                continue
            }
            v.visibility = View.VISIBLE

            //modif image oeuf le plus proche (taille et opacité)
            if (v == winner && minDistance < 450f) {
                if (v.scaleX != 1.3f) {
                    v.scaleX = 1.3f
                    v.scaleY = 1.3f
                    v.alpha = 1.0f
                    animatable?.start()
                }
            } else {
                if (v.scaleX != 0.8f) {
                    v.scaleX = 0.8f
                    v.scaleY = 0.8f
                    v.alpha = 0.5f
                    animatable?.stop()
                }
            }
        }
        selectedEgg = if (minDistance < 450f) winner else null
    }

    //Pour + opti, on clean les oeufs avec leur gifs
    fun cleanUpResources() {
        for (i in 0 until ui.eggsContainer.childCount) {
            val v = ui.eggsContainer.getChildAt(i)
            val eggImage = v.findViewById<ImageView>(R.id.imageView5)
            Glide.with(ui.context).clear(eggImage)
        }
        ui.eggsContainer.visibility = View.GONE
    }

    //applique la 'tapisserie' du panorama
    private fun setupPanorama() {
        val bitmap = BitmapFactory.decodeResource(ui.context.resources, R.drawable.back_pull) ?: return
        val scale = ui.backgroundImage.height.toFloat() / bitmap.height.toFloat()
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, ui.backgroundImage.height, true)

        imgWidth = scaledWidth.toFloat()
        panoramaShader = BitmapShader(scaledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        shaderPaint = Paint().apply {
            shader = panoramaShader
            isAntiAlias = true
        }

        ui.backgroundImage.setImageDrawable(object : Drawable() {
            override fun draw(canvas: Canvas) {
                shaderPaint?.let { canvas.drawPaint(it) }
            }
            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun getOpacity() = PixelFormat.TRANSLUCENT
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    //gestion du fling ou boussole
    fun toggleMode() {
        isSensorMode = !isSensorMode
        //mode boussole
        if (isSensorMode) {
            ui.boussole.alpha = 1.0f
            ui.backgroundImage.removeCallbacks(flingRunnable)
            start()
        } else {
            //mode fling
            ui.boussole.alpha = 0.5f
            stop()
        }
    }

    //gestion fling
    fun handleTouch(event: MotionEvent) {
        if (isSensorMode) return
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                ui.backgroundImage.removeCallbacks(flingRunnable)
                lastTouchX = event.x
                flingVelocity = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - lastTouchX
                flingVelocity = deltaX
                lastTouchX = event.x
                currentDegree -= deltaX * sensiScroll
                renderPanorama()
            }
            MotionEvent.ACTION_UP -> {
                if (abs(flingVelocity) > 1f) {
                    ui.backgroundImage.post(flingRunnable)
                }
            }
        }
    }

    fun start() {
        if (isSensorMode) {
            sensorManager.registerListener(this, compas, SensorManager.SENSOR_DELAY_UI)
        }
    }
    fun stop() = sensorManager.unregisterListener(this)
}