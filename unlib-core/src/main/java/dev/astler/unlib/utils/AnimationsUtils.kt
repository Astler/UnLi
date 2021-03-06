package dev.astler.unlib.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

fun View.shakeView() {
    val animator1 = ObjectAnimator.ofFloat(this, "translationX", -50f)
    animator1.repeatCount = 0
    animator1.duration = 50

    val animator2 = ObjectAnimator.ofFloat(this, "translationX", 50f)
    animator2.repeatCount = 0
    animator2.duration = 50

    val animator3 = ObjectAnimator.ofFloat(this, "translationX", 0f)
    animator3.repeatCount = 0
    animator3.duration = 50

    val set = AnimatorSet()
    set.play(animator1).before(animator2)
    set.play(animator2).before(animator3)
    set.start()
}