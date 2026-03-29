package com.example.idlemon

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView

object BattleAnimator {

    // NOUVEAU : Ajout du paramètre scaleTarget (1f par défaut)
    fun animateEntry(view: View, isPlayer: Boolean, scaleTarget: Float = 1f) {
        view.translationX = if (isPlayer) -500f else 500f
        view.alpha = 0f
        view.scaleX = 0.5f
        view.scaleY = 0.5f
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .scaleX(scaleTarget) // Vise la taille finale cible (1.4f pour le boss)
            .scaleY(scaleTarget)
            .setDuration(800)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun animateSwitchOut(view: View, onEnd: () -> Unit) {
        view.animate()
            .translationX(-500f)
            .alpha(0f)
            .setDuration(500)
            .withEndAction(onEnd)
            .start()
    }

    fun animateAttackMove(view: View, isPlayer: Boolean, onImpact: () -> Unit) {
        view.animate()
            .translationX(if (isPlayer) 300f else -300f)
            .translationY(if (isPlayer) -100f else 100f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                onImpact()
                view.animate()
                    .translationX(0f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }.start()
    }

    fun animateHit(view: View) {
        view.animate()
            .translationX(20f)
            .setDuration(50)
            .withEndAction {
                view.animate()
                    .translationX(-20f)
                    .setDuration(50)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(50)
                            .start()
                    }.start()
            }.start()
    }

    fun animateKO(view: View, pokemon: Pokemon, onEnd: () -> Unit) {
        MusicManager.crierPokemon(pokemon)

        Handler(Looper.getMainLooper()).postDelayed({
            MusicManager.jouerSonBattle("ko_sound")

            view.animate()
                .translationY(150f)
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .rotation(360f)
                .setDuration(600)
                .withEndAction {
                    view.translationY = 0f
                    onEnd()
                }
                .start()
        }, 600)
    }

    fun animateCritShake(view: View) {
        val shakeX = ObjectAnimator.ofFloat(view, "translationX", 0f, -30f, 30f, -20f, 20f, -10f, 10f, 0f)
        val shakeY = ObjectAnimator.ofFloat(view, "translationY", 0f, -15f, 15f, -5f, 5f, 0f)

        shakeX.duration = 400
        shakeY.duration = 400

        shakeX.start()
        shakeY.start()

        // flash blanc
        if (view is ImageView) {
            view.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            Handler(Looper.getMainLooper()).postDelayed({
                view.clearColorFilter()
            }, 100)
        }
    }

    fun animateBuff(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f)

        scaleX.duration = 400
        scaleY.duration = 400
        scaleX.interpolator = DecelerateInterpolator()
        scaleY.interpolator = DecelerateInterpolator()
        if (view is ImageView) {
            view.setColorFilter(Color.argb(100, 100, 200, 255), PorterDuff.Mode.SRC_ATOP)
            Handler(Looper.getMainLooper()).postDelayed({ view.clearColorFilter() }, 400)
        }

        scaleX.start()
        scaleY.start()
    }

    fun animateDebuff(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.8f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.8f, 1.0f)

        scaleX.duration = 400
        scaleY.duration = 400
        scaleX.interpolator = DecelerateInterpolator()
        scaleY.interpolator = DecelerateInterpolator()
        if (view is ImageView) {
            view.setColorFilter(Color.argb(100, 150, 0, 150), PorterDuff.Mode.SRC_ATOP)
            Handler(Looper.getMainLooper()).postDelayed({ view.clearColorFilter() }, 400)
        }

        scaleX.start()
        scaleY.start()
    }
}