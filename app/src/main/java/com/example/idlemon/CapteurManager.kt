package com.example.idlemon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
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
import kotlin.math.abs

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

    // Mode Tactile vs Capteur
    private var isSensorMode = true
    private var lastTouchX = 0f

    // Variables pour l'effet roulette
    private var flingVelocity = 0f
    private val friction = 0.7f // Décélération (plus proche de 1 = plus ça glisse longtemps)
    private val sensiScroll = 0.2f //sensi du sling

    // Runnable qui gère l'inertie
    private val flingRunnable = object : Runnable {
        override fun run() {
            if (abs(flingVelocity) < 0.5f) return // On arrête si c'est trop lent

            // On applique la vitesse actuelle
            currentDegree -= flingVelocity * sensiScroll

            // On réduit la vitesse pour la prochaine frame (frottement)
            flingVelocity *= friction

            renderPanorama()

            // On boucle tant qu'il y a de la vitesse (approx 60fps)
            activity.backgroundImage.postDelayed(this, 16)
        }
    }

    init {
        //config de la vue en 360
        activity.backgroundImage.post {
            setupPanorama()
            generateEggs()
        }
    }

    //bascule entre capteur ou fling
    fun toggleMode() {
        isSensorMode = !isSensorMode

        if (isSensorMode) {
            // Retour au mode sensor
            activity.boussole.alpha = 1.0f
            // On coupe l'inertie si on repasse en capteur pour pas que ça tourne tout seul
            activity.backgroundImage.removeCallbacks(flingRunnable)
            start()
        } else {
            // Passage en mode tactile
            activity.boussole.alpha = 0.5f
            stop() // On coupe le sensor
        }
    }

    //sling
    fun handleTouch(event: MotionEvent) {
        // ICI : On empêche le tactile si on est en mode capteur (boussole active)
        if (isSensorMode) return

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Si on pose le doigt, on arrête la roulette (catch)
                activity.backgroundImage.removeCallbacks(flingRunnable)
                lastTouchX = event.x
                flingVelocity = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - lastTouchX

                // On garde la vitesse en mémoire pour le lacher
                flingVelocity = deltaX
                lastTouchX = event.x

                //sensi du sling
                // On inverse (deltaX * -1) pour que le glissement soit naturel
                currentDegree -= deltaX * sensiScroll

                renderPanorama()
            }
            MotionEvent.ACTION_UP -> {
                // Au relâchement, si on a de la vitesse, on lance l'inertie
                if (abs(flingVelocity) > 1f) {
                    activity.backgroundImage.post(flingRunnable)
                }
            }
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
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION && isSensorMode) {

            //calculs rotations
            val targetDegree = event.values[0]
            var diff = targetDegree - currentDegree
            if (diff > 180) diff -= 360
            if (diff < -180) diff += 360
            currentDegree += diff * valFluide
            renderPanorama()
        }
    }

    //appelé par sensor et tactile pour faire la tapisserie du fond
    private fun renderPanorama() {
        if (imgWidth <= 0) return

        // Normalisation 0-360
        if (currentDegree >= 360) currentDegree %= 360
        if (currentDegree < 0) currentDegree = 360 + (currentDegree % 360)

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
                // On évite de relancer l'anim si déjà actif
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
        //450 est a peu près bien ici
        selectedEgg = if (minDistance < 450f) winner else null
    }

    fun cleanUpResources() {
        for (i in 0 until activity.eggsContainer.childCount) {
            val v = activity.eggsContainer.getChildAt(i)
            val eggImage = v.findViewById<ImageView>(R.id.imageView5)

            //on clear les gifs tournant en fond
            Glide.with(activity).clear(eggImage)
        }
        activity.eggsContainer.visibility = View.GONE
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}//pas utile

    fun start() {
        if (isSensorMode) {
            sensorManager.registerListener(this, compas, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() = sensorManager.unregisterListener(this)
}