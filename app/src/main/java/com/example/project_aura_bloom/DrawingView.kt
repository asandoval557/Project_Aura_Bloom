package com.example.project_aura_bloom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var paint = initializePaint()
    private var currentPath: CustomPath? = null
    private val drawingPaths = mutableListOf<Pair<Path, Paint>>()
    private val removedPaths = mutableListOf<Pair<Path, Paint>>()
    private var brushColor = Color.BLACK
    private var eraserColor = Color.WHITE
    private var eraserThickness = 30f
    private var brushThickness = 10f
    var isEraserMode = false

    private fun initializePaint() = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = brushThickness
        isAntiAlias = true

    }

    init {
        setupPaint()
    }

    private fun setupPaint() {
        paint = initializePaint().apply {
            color = if (isEraserMode) eraserColor else brushColor
            strokeWidth = if (isEraserMode) eraserThickness else brushThickness
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((path, paint) in drawingPaths) {
            canvas.drawPath(path, paint)
        }
        currentPath?.let { canvas.drawPath(it, paint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startPath(x, y)
            MotionEvent.ACTION_MOVE -> continuePath(x, y)
            MotionEvent.ACTION_UP -> endPath(x, y)
            else -> return false
        }
        return true
    }

    fun startPath(x: Float, y: Float) {
        currentPath = CustomPath(brushColor, brushThickness).apply {
            moveTo(x, y)
        }
    }

    fun continuePath(x: Float, y: Float) {
        currentPath?.let {
            it.lineTo(x, y)
            invalidate()
        }
    }

    fun endPath(x: Float, y: Float) {
        currentPath?.let {
            it.lineTo(x, y)
            drawingPaths.add(Pair(it, Paint(paint)))
        }
        currentPath = null
        invalidate()
    }

    fun setEraseMode() {
        isEraserMode = true
        invalidate()
    }

    fun exitEraseMode() {
        isEraserMode = false
        setupPaint()
        invalidate()
    }

    fun setEraseSize(size: Float) {
        eraserThickness = size
        if (isEraserMode) {
            paint.strokeWidth = size
        }
        invalidate()
    }

    fun undo() {
        if (currentPath != null) {
            currentPath = null
            invalidate()
        } else if (drawingPaths.isNotEmpty()) {
            removedPaths.add(drawingPaths.removeAt(drawingPaths.size - 1))
        }
        invalidate()
    }

    fun redo() {
        if (removedPaths.isNotEmpty()) {
            drawingPaths.add(removedPaths.removeAt(removedPaths.size - 1))
            invalidate()
        }
    }

    fun setBrushSize(size: Float) {
        brushThickness = size
        if (!isEraserMode) {
            paint.strokeWidth = size
        }
        invalidate()
    }

    fun setBrushColor(color: Int) {
        brushColor = color
        if (!isEraserMode) {
            paint.color = color
        }
        invalidate()
    }

    fun setEraserColor(color: Int) {
        eraserColor = color
        if (isEraserMode) {
            paint.color = color
        }
        invalidate()
    }

    fun saveDrawing() {
        TODO("Not yet implemented")
    }

    fun shareDrawing() {
        TODO("Not yet implemented")
    }

    fun clear() {
        drawingPaths.clear()
        removedPaths.clear()
        invalidate()
    }
}

internal class CustomPath(var color: Int, var brushThickness: Float) : Path()

