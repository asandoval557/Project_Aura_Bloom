package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController

class HelpCenterFragment : Fragment(R.layout.fragment_help_center) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.termsButton).setOnClickListener {
            findNavController().navigate(R.id.action_HelpCenterFragment_to_TermsFragment)
        }

        view.findViewById<Button>(R.id.privacyButton).setOnClickListener {
            findNavController().navigate(R.id.action_HelpCenterFragment_to_PrivacyPolicyFragment)
        }
    }
}