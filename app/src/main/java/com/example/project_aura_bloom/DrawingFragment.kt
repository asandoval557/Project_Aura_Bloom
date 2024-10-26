package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ImageButton
import com.example.project_aura_bloom.databinding.MindfulArtFragmentBinding



class DrawingFragment : Fragment() {

    private lateinit var drawingView: DrawingView
    private var isMenuOpen = false
    private var _binding: MindfulArtFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MindfulArtFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        drawingView = view.findViewById(R.id.drawing_canvas)!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val brushSizeButton: ImageButton = view
            .findViewById(R.id.brush_size_button)
        brushSizeButton.setOnClickListener{
            showBrushSizeMenu(it)
        }

        val eraseSizeButton: ImageButton = view
            .findViewById(R.id.erase_size_button)
        eraseSizeButton.setOnClickListener{
            // Handle erase size
            drawingView.setEraseMode()
        }

        val undoButton: ImageButton = view
            .findViewById(R.id.undo_button)
        undoButton.setOnClickListener{
            // Handle undo
            drawingView.undo()
        }

        val redoButton: ImageButton = view
            .findViewById(R.id.redo_button)
        redoButton.setOnClickListener{
            // Handle redo
            drawingView.redo()
        }

        val colorPickerButton: ImageButton = view
            .findViewById(R.id.color_picker_button)
        colorPickerButton.setOnClickListener{
            // Handle color picker
            openColorPickerDialog()
        }

        val clearButton: ImageButton = view
            .findViewById(R.id.clear_button)
        clearButton.setOnClickListener{
            // Handle clear
            drawingView.clear()
        }

        val arrowMenuButton: ImageButton = view
            .findViewById(R.id.arrow_menu_button)
        arrowMenuButton.setOnClickListener{
            showSaveShareMenu(it)
        }

        return
    }



    private fun showBrushSizeMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.brush_size_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.brush_large -> {
                    // Handle large brush size
                    drawingView.setBrushSize(BrushSize.LARGE_BRUSH_SIZE)
                    true
                }
                R.id.brush_medium -> {
                    // Handle medium brush size
                    drawingView.setBrushSize(BrushSize.MEDIUM_BRUSH_SIZE)
                    true
                }
                R.id.brush_large -> {
                    // Handle small brush size
                    drawingView.setBrushSize(BrushSize.SMALL_BRUSH_SIZE)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun openColorPickerDialog() {
        TODO("Not yet implemented")
    }

    private fun showSaveShareMenu(view: View) {
        val menuButton: ImageButton = view as ImageButton
        val popup = PopupMenu(requireContext(), menuButton)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.save_share_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    // Handle save
                    drawingView.saveDrawing()
                    true
                }
                R.id.share -> {
                    // Handle share
                    drawingView.shareDrawing()
                    true
                }
                else -> false
            }
        }
        popup.setOnDismissListener{
            isMenuOpen = false
            menuButton.setImageResource(R.drawable
                .arrow_up_24dp)
        }

        if (isMenuOpen) {
            popup.dismiss()
        } else {
            popup.show()
            menuButton.setImageResource(R.drawable
                .arrow_down_24dp)
        }
        isMenuOpen = !isMenuOpen
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