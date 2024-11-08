package com.example.project_aura_bloom

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_aura_bloom.databinding.MindfulArtFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        drawingView.updateBrushColor(ContextCompat.getColor(requireContext(), android.R.color.black))
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
        val colors = arrayOf("#000000", "#E91E63", "#9C27B0",
            "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B",
            "#FFC107", "#FF9800", "#F44336")

        var selectedColor: Int? = null

        val colorPickerAdapter = ColorPickerAdapter(colors) { color ->
            selectedColor = color
        }
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = colorPickerAdapter
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose a color")
            .setView(recyclerView)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Ok") { _, _ ->
                selectedColor?.let {
                    drawingView.updateBrushColor(it)
                }
            }
            .create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ColorPickerAdapter(private val colors: Array<String>, private val onColorSelected: (Int) -> Unit) :
    RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.color_picker_menu, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val color = Color.parseColor(colors[position])
        holder.colorView.setBackgroundColor(color)
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
            onColorSelected(color)
        }

        holder.colorBorder.visibility = if (position == selectedPosition)
            View.VISIBLE else View.GONE

    }


    override fun getItemCount(): Int = colors.size

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: ImageView = view.findViewById(R.id.color_view)
        val colorBorder: View = view.findViewById(R.id.color_border)
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