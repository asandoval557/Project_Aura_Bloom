package com.example.project_aura_bloom

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint = initializePaint()
    private var currentPath: CustomPath? = null
    private val drawingPaths = mutableListOf<Pair<Path, Paint>>()
    private val removedPaths = mutableListOf<Pair<Path, Paint>>()
    private var brushColor = Color.BLACK
    private var eraserColor = Color.WHITE
    private var eraserThickness = 30f
    private var brushThickness = 10f
    var isEraserMode = false
    private var backgroundBitmap: Bitmap? = null

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
        backgroundBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        for ((path, paint) in drawingPaths) {
            canvas.drawPath(path, paint)
        }
        currentPath?.let { canvas.drawPath(it, paint) }
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

    fun updateBrushColor(color: Int) {
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

    fun saveDrawing(): Uri? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)

        return try{
            val file = File(context.cacheDir, "drawing${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            Toast.makeText(context, "Drawing saved at: ${file.path}", Toast.LENGTH_LONG).show()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving drawing", Toast.LENGTH_SHORT).show()
            null
        }

    }

    fun shareDrawing() {
        val imageUri = saveDrawing()
        if (imageUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/png"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share using the options"))
        } else {
            Toast.makeText(context, "Failed to save and share", Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun clear() {
        drawingPaths.clear()
        removedPaths.clear()
        invalidate()
    }

    fun addPhoto(photoResId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, photoResId)
        backgroundBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        invalidate()
    }

    fun clearPhoto() {
        backgroundBitmap = null
        invalidate()
    }
}

internal class CustomPath(var color: Int, var brushThickness: Float) : Path()

