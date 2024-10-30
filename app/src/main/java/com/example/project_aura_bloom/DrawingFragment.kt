package com.example.project_aura_bloom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.project_aura_bloom.databinding.MindfulArtFragmentBinding

class DrawingFragment : Fragment() {
    private lateinit var drawingView: DrawingView
    private var isMenuOpen = false
    private var _binding: MindfulArtFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ITEM_SAVED = true
        private const val ITEM_NOT_SAVED = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MindfulArtFragmentBinding.inflate(inflater, container, false)
        drawingView = binding.drawingCanvas

        setupTouchListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeButtonListeners()
    }

    private fun initializeButtonListeners() {
        with(binding) {
            setButtonListener(brushSizeButton, R.menu.brush_size_menu) { handleBrushSizeSelection(it.itemId) }
            setButtonListener(eraseSizeButton, R.menu.eraser_size_menu) { handleEraserSizeSelection(it.itemId) }
            undoButton.setOnClickListener { drawingView.undo() }
            redoButton.setOnClickListener { drawingView.redo() }
            colorPickerButton.setOnClickListener { openColorPickerDialog() }
            clearButton.setOnClickListener { drawingView.clear() }
            arrowMenuButton.setOnClickListener { showSaveShareMenu(it) }
        }
    }

    private fun setButtonListener(button: View, menuRes: Int, onMenuItemClick: (MenuItem) -> Boolean) {
        button.setOnClickListener {
            showPopupMenu(it, menuRes, onMenuItemClick)
        }
    }

    private fun showPopupMenu(view: View, menuRes: Int, onMenuItemClick: (MenuItem) -> Boolean) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(menuRes, menu)
            setForceShowIcon(this)
            setOnMenuItemClickListener(onMenuItemClick)
            show()
        }
    }

    private fun handleBrushSizeSelection(itemId: Int): Boolean {
        val brushSize = when (itemId) {
            R.id.brush_large -> BrushSize.LARGE_BRUSH_SIZE
            R.id.brush_medium -> BrushSize.MEDIUM_BRUSH_SIZE
            R.id.brush_small -> BrushSize.SMALL_BRUSH_SIZE
            else -> return ITEM_NOT_SAVED
        }
        drawingView.setBrushSize(brushSize)
        drawingView.setBrushColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        binding.eraserCircle.visibility = View.GONE
        drawingView.exitEraseMode()
        return ITEM_SAVED
    }

    private fun handleEraserSizeSelection(itemId: Int): Boolean {
        val eraseSize = when (itemId) {
            R.id.eraser_large -> EraseSize.LARGE_ERASE_SIZE
            R.id.eraser_medium -> EraseSize.MEDIUM_ERASE_SIZE
            R.id.eraser_small -> EraseSize.SMALL_ERASE_SIZE
            else -> return ITEM_NOT_SAVED
        }
        drawingView.apply {
            setEraseSize(eraseSize)
            setEraseMode()
            setEraserColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        binding.eraserCircle.apply {
            updateRadius(eraseSize)
            visibility = View.VISIBLE
        }
        return ITEM_SAVED
    }

    private fun setForceShowIcon(popup: PopupMenu) {
        try {
            val fields = popup.javaClass.declaredFields
            fields.filter { it.name == "mPopup" }.forEach { field ->
                field.isAccessible = true
                val menuPopupHelper = field.get(popup)
                val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                setForceIcons.invoke(menuPopupHelper, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        binding.drawingCanvas.setOnTouchListener { _, event ->
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    drawingView.startPath(x, y)
                    if (drawingView.isEraserMode) {
                        binding.eraserCircle.updatePosition(x, y)
                        binding.eraserCircle.visibility = View.VISIBLE
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    drawingView.continuePath(x, y)
                    if (drawingView.isEraserMode) {
                        binding.eraserCircle.updatePosition(x, y)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    drawingView.endPath(x, y)
                    if (!drawingView.isEraserMode) {
                        binding.eraserCircle.visibility = View.GONE
                    }
                }

                else -> return@setOnTouchListener false
            }
            true
        }
    }

    private fun showSaveShareMenu(view: View) {
        val menuButton = view as ImageButton
        PopupMenu(requireContext(), menuButton).apply {
            menuInflater.inflate(R.menu.save_share_menu, menu)
            setForceShowIcon(this)
            setOnMenuItemClickListener { handleSaveShareSelection(it.itemId) }
            setOnDismissListener {
                isMenuOpen = false
                menuButton.setImageResource(R.drawable.arrow_up_24dp)
            }
            if (isMenuOpen) dismiss() else show()
        }
        menuButton.setImageResource(if (!isMenuOpen) R.drawable.arrow_down_24dp else R.drawable.arrow_up_24dp)
        isMenuOpen = !isMenuOpen
    }

    private fun handleSaveShareSelection(itemId: Int): Boolean {
        return when (itemId) {
            R.id.save -> {
                drawingView.saveDrawing()
                ITEM_SAVED
            }

            R.id.share -> {
                drawingView.shareDrawing()
                ITEM_SAVED
            }

            else -> ITEM_NOT_SAVED
        }
    }

    private fun openColorPickerDialog() {
        TODO()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

object BrushSize {
    const val LARGE_BRUSH_SIZE = 30f
    const val MEDIUM_BRUSH_SIZE = 20f
    const val SMALL_BRUSH_SIZE = 10f
}

object EraseSize {
    const val LARGE_ERASE_SIZE = 90f
    const val MEDIUM_ERASE_SIZE = 60f
    const val SMALL_ERASE_SIZE = 30f
}