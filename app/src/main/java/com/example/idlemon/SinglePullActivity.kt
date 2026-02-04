package com.example.idlemon

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SinglePullActivity : AppCompatActivity() {
    private lateinit var capteurManager: CapteurManager
    lateinit var backgroundImage: ImageView
    lateinit var eggsContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_pull)

        backgroundImage = findViewById(R.id.background360)
        eggsContainer = findViewById(R.id.eggsContainer)

        capteurManager = CapteurManager(this)

        val pokemonPull = DataManager.model.getRandomPokemon()
        Player.addPokemon(pokemonPull)
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

class CapteurManager(private val activity: SinglePullActivity) : SensorEventListener {
    private val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val compas = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    private var imgWidth = 0f
    private val view360 = Matrix()
    private var panoramaShader: BitmapShader? = null
    private var shaderPaint: Paint? = null

    private var currentDegree = 0f
    private val valFluide = 0.15f

    init {
        //On cree le fond qui tourne avec boussole
        activity.backgroundImage.post {
            val bitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.back_pull) ?: return@post

            val scale = activity.backgroundImage.height.toFloat() / bitmap.height.toFloat()
            val scaledWidth = (bitmap.width * scale).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, activity.backgroundImage.height, true)

            imgWidth = scaledWidth.toFloat()

            panoramaShader = BitmapShader(scaledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
            shaderPaint = Paint().apply {
                shader = panoramaShader
                isAntiAlias = true
            }
            activity.backgroundImage.setImageDrawable(object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: Canvas) { canvas.drawPaint(shaderPaint!!) }
                override fun setAlpha(alpha: Int) {}
                override fun setColorFilter(cf: ColorFilter?) {}
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            })
            generateEggs()
        }
    }

    private fun generateEggs() {
        activity.eggsContainer.removeAllViews()
        for (set in -1..1) {
            val offsetSet = set * imgWidth

            for (i in 0 until 5) { //spawn 5 oeufs
                val egg = ImageView(activity)
                com.bumptech.glide.Glide.with(activity)
                    .asGif()
                    .load(R.drawable.egg)
                    .centerCrop()
                    .into(egg)

                val size = 700 //a peu pres bien en vrai
                val params = FrameLayout.LayoutParams(size, size)
                egg.layoutParams = params
                //place à 1/5 de tour
                val xPosition = (i.toFloat() / 5f * imgWidth) + offsetSet
                egg.x = xPosition
                //centre en y
                egg.y = (activity.backgroundImage.height / 2f) - (size / 2f)
                egg.setOnClickListener {
                    //event récup oeuf
                    it.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200).start()
                }
                activity.eggsContainer.addView(egg)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION && imgWidth > 0) {
            val targetDegree = event.values[0]
            var diff = targetDegree - currentDegree
            if (diff > 180) diff -= 360
            if (diff < -180) diff += 360
            //on boucle modulo 360
            currentDegree += diff * valFluide
            if (currentDegree >= 360) currentDegree -= 360
            if (currentDegree < 0) currentDegree += 360

            //entre 0 et 1
            val fraction = currentDegree / 360f
            val scrollX = fraction * imgWidth

            view360.setTranslate(-scrollX, 0f)
            panoramaShader?.setLocalMatrix(view360)
            activity.backgroundImage.invalidate()
            val screenCenter = activity.backgroundImage.width / 2f
            activity.eggsContainer.translationX = -scrollX + screenCenter - 100f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} //Pas util
    fun start() = sensorManager.registerListener(this, compas, SensorManager.SENSOR_DELAY_GAME)
    fun stop() = sensorManager.unregisterListener(this)
}