package com.example.project_aura_bloom

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.Canvas
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.net.Uri

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

    fun saveDrawing(): Uri? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)

        return try{
            val file = File(context.cacheDir, "drawing_${System.currentTimeMillis()}.png")
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
            Toast.makeText(context, "Failed to save and share", Toast.LENGTH_SHORT).show()
        }
    }


}