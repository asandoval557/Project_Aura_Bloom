package com.example.project_aura_bloom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class EraserCircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var radius: Float = 50f
    private var circleX: Float = 0f
    private var circleY: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(circleX, circleY, radius, paint)
    }

    fun updatePosition(x: Float, y: Float) {
        circleX = x
        circleY = y
        invalidate()
    }

    fun updateRadius(newRadius: Float) {
        radius = newRadius
        invalidate()
    }
}