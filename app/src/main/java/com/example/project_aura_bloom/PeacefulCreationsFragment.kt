package com.example.project_aura_bloom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_aura_bloom.databinding.PeacefulCreationsHubBinding

class PeacefulCreationsFragment : Fragment() {

        // Binding safely and access to the peaceful creations layout
    private var _binding: PeacefulCreationsHubBinding? = null
    private val binding get() = _binding!!

        // Inflating the layout, and setting up the root view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = PeacefulCreationsHubBinding.inflate(inflater,container,false)
        return binding.root
    }

        // Setting up the view once everything has been created and displayed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            // If button below is clicked within peaceful creations hub then...
        binding.lumosPathButton.setOnClickListener {
                // Screen switches to the lumos activity screen
            findNavController().navigate(R.id.action_peaceful_creations_to_lumosPathFragment)
        }
    }

        // Binding clean up when view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

