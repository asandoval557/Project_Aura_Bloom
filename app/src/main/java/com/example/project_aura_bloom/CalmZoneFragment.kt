package com.example.project_aura_bloom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_aura_bloom.databinding.CalmZoneScreenBinding

class CalmZoneFragment: Fragment() {

        // Binding safely and access to the calm zone screen UI elements
    private var _binding : CalmZoneScreenBinding? = null
    private val binding get() = _binding!!

        // Inflating the layout, and setting up the view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CalmZoneScreenBinding.inflate(inflater,container,false)
        return binding.root
    }

        // Setting up the view once everything has been created and displayed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the Welcome Message animation
        val fadeInLeftToRight = AnimationUtils.loadAnimation(activity,R.anim.fade_in_left_to_right)

        // Apply the animation to the welcome message TextView
        binding.calmZoneInto.startAnimation(fadeInLeftToRight)

        // If button below is clicked within calm zone hub then...
        binding.peacefulCreationsButton.setOnClickListener {
                // Screen switches to the peaceful creations hub
            findNavController().navigate(R.id.action_calm_zone_to_peaceful_creations)
        }

        binding.meditationButton.setOnClickListener {
            findNavController().navigate(R.id.action_calm_zone_to_Meditation)
        }
    }

        // Binding clean up when view is destroyed to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}