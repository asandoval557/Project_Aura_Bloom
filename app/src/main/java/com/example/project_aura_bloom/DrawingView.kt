package com.example.project_aura_bloom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

   private val paint = Paint().apply {
      color = Color.BLACK
      style = Paint.Style.STROKE
      strokeWidth = 10f
      isAntiAlias = true
   }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent
                .ACTION_POINTER_DOWN -> {
                path.moveTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val pressure = event.pressure
                val pointerCount = event.pointerCount

                paint.strokeWidth = 10f * pressure

                for (i in 0 until pointerCount) {
                    val px = event.getX(i)
                    val py = event.getY(i)
                    path.lineTo(px, py)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // finalize the path
                path.lineTo(x, y)
                invalidate()
            }
            else -> return false
        }

        return true
    }

    fun setEraseMode() {
        paint.color = Color.WHITE
    }

    fun undo() {
        TODO("Not yet implemented")
    }

    fun redo() {
        TODO("Not yet implemented")
    }

    fun setBrushSize(fl: Float) {
        paint.strokeWidth = fl
    }

    fun setBrushColor(color: Int) {
        paint.color = color
    }

    fun saveDrawing() {
        TODO("Not yet implemented")
    }

    fun shareDrawing() {
        TODO("Not yet implemented")
    }


}