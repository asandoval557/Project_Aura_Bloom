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
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityTokenResponse
import java.util.UUID
import android.util.Log


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


        binding.tvForgotPassword.setOnClickListener {
            // Retrieve email input from the email EditText field
            val email = binding.etEmail.text.toString()

            // Validate: Check if email field is empty
            if(email.isEmpty()) {
                // Display warning message if email is empty
                Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show()
                // Exit the click listener if validation fails
                return@setOnClickListener
            }
            // Initiate password reset process through Firebase Authentication
            // This allows users to reset their password directly from the login screen
            // without navigating to a separate reset password screen
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        // Notify user that reset email has been sent successfully
                        Toast.makeText(requireContext(), "Check your email for reset password link", Toast.LENGTH_SHORT).show()
                    } else {
                        // Notify user if the email is not found in the system
                        Toast.makeText(requireContext(), "Email not found, check your email and try again", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Handle "Sign Up" text click to navigate to SignUpFragment
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.montserrat_alternates_regular)
        binding.tvSignUp.typeface = customFont
    }

    // Simplified log in for the user with firebase Authentication
    private fun loginUser(email: String, password: String) {
        // Attempt to authenticate user with provided email and password using Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Check if authentication was successful
                if (task.isSuccessful) {
                    // Display success message to user
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    // Navigate user to the Home Screen after successful login
                    findNavController().navigate(R.id.action_LoginFragment_to_HomeScreenFragment)
                } else {
                    // Display error message if login fails, including the specific error message
                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}