package com.example.project_aura_bloom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    // Binding clean up when view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}