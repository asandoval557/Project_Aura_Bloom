package com.example.project_aura_bloom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator
import kotlin.math.min

class CloudOfColor @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): View(context, attrs) {

        // Storing the touch positions and the current color of the cloud
    private var touchX = -1f
    private var touchY = -1f
    private var currentColor = Color.WHITE
    private var cloudRad = 200f

        // Defining how the cloud will be drawn
    private val paint = Paint()

        // Creating the filled shape and smoothing out the edges of the cloud
    init {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
    }

        // Creating the cloud effect when the view is redrawn
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

            // If touch point is within frame drawing of the radical gradient will occur
        if (touchX != -1f && touchY != -1f) {
            val  innerGradient = RadialGradient(
                touchX, touchY, cloudRad/2,
                Color.argb(255, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor)),
                Color.argb(0,255,255,255),
                Shader.TileMode.CLAMP
            )

            val outerGradient = RadialGradient(
                touchX, touchY, cloudRad,
                Color.argb(150, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor)),
                Color.argb(0,255,255,255),
                Shader.TileMode.CLAMP
            )

            paint.shader = innerGradient
            canvas.drawCircle(touchX, touchY, cloudRad/2, paint)

                // Cloud being created in a form of a circle
            paint.shader = outerGradient
            canvas.drawCircle(touchX, touchY, cloudRad, paint)
        }
    }

        // Updating the position of the cloud inorder to create a redraw
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

            // When the touch starts or move the coordinates and colors will update
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y

                    // Random color being generated with each movement
                    // Expanding the cloud on touch
                animateColorTransition()
                expandCloud()
            }

                // When touch of the finger has been removed from device the cloud position will reset and be redrawn
            MotionEvent.ACTION_UP -> {
                touchX = -1f
                touchY = -1f
                cloudRad = 200f
                invalidate()
            }
        }
        return true
    }

        // Generates the random colors when tracing of finger occurs
    private fun generateRandomColor(): Int {
        val random = java.util.Random()
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    private fun animateColorTransition() {
        val colorFrom = currentColor
        val colorTo = generateRandomColor()

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)

        colorAnimator.duration = 2000
        colorAnimator.addUpdateListener { animator ->
            currentColor = animator.animatedValue as Int
            invalidate()
        }
        colorAnimator.start()
    }

    private fun expandCloud() {
        val animator = ValueAnimator.ofFloat(200f, 500f)
        animator.duration = 1500
        animator.addUpdateListener { animator ->
            cloudRad = animator.animatedValue as Float
            invalidate()
        }
        animator.start()
    }
}