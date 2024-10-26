package com.example.project_aura_bloom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

   private var paint = Paint().apply {
      color = Color.BLACK
      style = Paint.Style.STROKE
      strokeWidth = 10f
        isAntiAlias = true
   }

    private var path = Path()
    private var drawPath: CustomPath? = null
    //List to store all history of the paths
    private val paths = mutableListOf<Pair<Path, Paint>>()
    private val undonePaths = mutableListOf<Pair<Path, Paint>>()

    private var color = Color.BLACK
    private var brushThickness = 10f

    init {
        setupPaint()
    }
    private fun setupPaint() {
            paint = Paint().apply {
               color = this@DrawingView.color
                style = Paint.Style.STROKE
                strokeWidth = this@DrawingView.brushThickness
                isAntiAlias = true
            }
        drawPath = CustomPath(color, brushThickness)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((path, paint) in paths) canvas.drawPath(path, paint)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, -> {
                path = Path().apply {moveTo(x, y) }
                paths.add(Pair(path, Paint(paint)))
                undonePaths.clear()
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
        invalidate()
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            undonePaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    fun redo() {
        if (undonePaths.isNotEmpty()) {
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
    }

    fun setBrushSize(fl: Float) {
        paint.strokeWidth = fl
        invalidate()
    }

    fun setBrushColor(color: Int) {
        this.color = color
        paint.color = color
        invalidate()
    }

    fun saveDrawing() {
        TODO("Not yet implemented")
    }

    fun shareDrawing() {
        TODO("Not yet implemented")
    }

    fun clear() {
        paths.clear()
        undonePaths.clear()
        invalidate()
    }


}

internal class CustomPath(var color: Int, var brushThickness: Float) : Path()
