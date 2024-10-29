package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ImageButton
import com.example.project_aura_bloom.databinding.MindfulArtBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DrawingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DrawingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var drawingView: DrawingView
    private var isMenuOpen = false
    private var _binding: MindfulArtBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MindfulArtBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_drawing,
        // container, false)


        drawingView = view.findViewById(R.id.mindful_art)

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

        val arrowMenuButton: ImageButton = view
            .findViewById(R.id.arrow_menu_button)
        arrowMenuButton.setOnClickListener{
            showSaveShareMenu(it)
        }

        return view
    }



    private fun showBrushSizeMenu(view: View) {
        val popup = PopupMenu(context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.brush_size_menu, popup.menu)

        // Force icons to show in the popup menu
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if (field.name == "mPopup") {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.brush_small -> {
                    // Handle small brush size
                    drawingView.setBrushSize(5f)
                    true
                }
                R.id.brush_medium -> {
                    // Handle medium brush size
                    drawingView.setBrushSize(10f)
                    true
                }
                R.id.brush_large -> {
                    // Handle large brush size
                    drawingView.setBrushSize(20f)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DrawingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DrawingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}