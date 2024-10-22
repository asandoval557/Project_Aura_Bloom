package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import com.example.project_aura_bloom.R
import com.example.project_aura_bloom.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    //Implement firebase authentication variable
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize Firebase Authentication
        auth = Firebase.auth

        // Handle "Log In" button click
        binding.btnLogIn.setOnClickListener {
            // TODO: Handle Login logic here
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            //Check if user has entered email and password
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Log in the user
            loginUser(email, password)
        }

        // Handle "Sign Up" text click to navigate to SignUpFragment
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.montserrat_alternates_bold)
        binding.tvSignUp.typeface = customFont
    }

    //Function to log in the user with Firebase Authentication
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //User successfully logged in
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    //TODO Need to adjust the navigation to the name of the next screen
                    //findNavController().navigate(R.id.action_LoginFragment_to_HomeFragment)
                } else {
                    //If sign in fails, alert the user
                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}