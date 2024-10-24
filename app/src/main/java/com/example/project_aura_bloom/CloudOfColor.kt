package com.example.project_aura_bloom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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
            val radialGradient = RadialGradient(
                touchX, touchY, cloudRad,

                    // Brightening the center color and creating a semi transparent edge
                Color.argb(255, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor)),
                Color.argb(50, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
            paint.shader = radialGradient

                // Cloud being created in a form of a circle
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
                currentColor = generateRandomColor()
                invalidate()
            }

                // When touch of the finger has been removed from device the cloud position will reset and be redrawn
            MotionEvent.ACTION_UP -> {
                touchX = -1f
                touchY = -1f
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
}