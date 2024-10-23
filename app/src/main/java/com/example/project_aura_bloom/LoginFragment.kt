package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.example.project_aura_bloom.R
import com.example.project_aura_bloom.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle "Log In" button click
        binding.btnLogIn.setOnClickListener {
            // TODO: Handle Login logic here (Replace with actual logic when Firebase is implemented)
            val isLoginSuccessful = handleLogin()

            if (isLoginSuccessful) {
                findNavController().navigate(R.id.action_LoginFragment_to_HomeScreenFragment)
            } else {
                // Handle login failure here
            }
        }

        // Handle "Sign Up" text click to navigate to SignUpFragment
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.montserrat_alternates_regular)
        binding.tvSignUp.typeface = customFont
    }

    private fun handleLogin(): Boolean {
        // TODO: Place actual logic here
        return true // Assuming successful login
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}