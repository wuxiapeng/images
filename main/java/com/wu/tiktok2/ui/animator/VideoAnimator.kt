package com.wu.tiktok2.ui.animator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.wu.tiktok2.R

object ViewAnimationUtils {

    /**
     * 1. 屏幕双击：在手指位置动态生成爱心并飘走
     */
    fun showHeartAtTouch(parent: ViewGroup, x: Float, y: Float) {
        val context = parent.context

        // 动态创建 ImageView
        val heartView = ImageView(context).apply {
            setImageResource(R.drawable.ic_heart_filled)
            setColorFilter(Color.parseColor("#FF4081")) // 粉色
            layoutParams = ViewGroup.LayoutParams(150, 150)
            translationX = x - 75
            translationY = y - 75
            rotation = (Math.random() * 40 - 20).toFloat() // 随机角度
        }

        parent.addView(heartView)

        // 组合动画
        val scaleX = ObjectAnimator.ofFloat(heartView, "scaleX", 0.5f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(heartView, "scaleY", 0.5f, 1.2f, 1.0f)
        val alpha = ObjectAnimator.ofFloat(heartView, "alpha", 1.0f, 0.0f)
        val translationY = ObjectAnimator.ofFloat(heartView, "translationY", y - 75, y - 250)

        AnimatorSet().apply {
            play(scaleX).with(scaleY)
            play(translationY).with(alpha).after(200)
            duration = 800
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    parent.removeView(heartView) // 结束后移除，防止内存泄漏
                }
            })
            start()
        }
    }

    /*
    * 2. 点赞按钮动画：缩放 + 变色 + Lottie爆炸
    */
    fun playLikeAnimation(ivLike: ImageView, lavExplosion: LottieAnimationView) {
        // 缩放
        val scaleX = ObjectAnimator.ofFloat(ivLike, "scaleX", 1.0f, 1.5f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(ivLike, "scaleY", 1.0f, 1.5f, 1.0f)
        scaleX.duration = 300
        scaleY.duration = 300
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()

        // 颜色渐变 (白 -> 红)
        val colorAnim = ValueAnimator.ofArgb(Color.WHITE, Color.parseColor("#FF4081"))
        colorAnim.duration = 300
        colorAnim.addUpdateListener { animation ->
            ivLike.setColorFilter(animation.animatedValue as Int)
        }

        // Lottie 播放
        lavExplosion.visibility = View.VISIBLE
        lavExplosion.playAnimation()
        lavExplosion.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                lavExplosion.visibility = View.GONE
            }
        })

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, colorAnim)
            start()
        }
    }

}