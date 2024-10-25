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
            requestIntegrityToken(email, password)
        }

        // Handle "Sign Up" text click to navigate to SignUpFragment
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.montserrat_alternates_regular)
        binding.tvSignUp.typeface = customFont
    }

    //Function to log in the user with Firebase Authentication

    private fun requestIntegrityToken(email: String, password: String) {
       val integrityManager = IntegrityManagerFactory.create(requireContext())

        //Generate a unique nonce
        val nonce = UUID.randomUUID().toString()

        //Create the token request
        val request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(860582708038)
            .setNonce(nonce)
            .build()

        integrityManager.requestIntegrityToken(request)
        .addOnSuccessListener { response: IntegrityTokenResponse ->
                val token = response.token()
                loginUser(email, password, token)
            }
        .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to request token: ${exception.message}", Toast.LENGTH_SHORT).show()
            loginWithoutIntegrityCheck(email, password)
        }
    }

    private fun loginWithoutIntegrityCheck(email: String, password: String) {
        Toast.makeText(context, "Proceeding without integrity check (DEV MODE)", Toast.LENGTH_SHORT).show()
        // Proceed with Firebase authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the next screen
                    findNavController().navigate(R.id.action_LoginFragment_to_HomeScreenFragment)
                } else {
                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Updating loginUser function to handle the token
    private fun loginUser(email: String, password: String, token: String?) {
        // Log token and proceed with login
        Log.d("IntegrityCheck", "Received integrity token: $token")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the next screen
                    findNavController().navigate(R.id.action_LoginFragment_to_HomeScreenFragment)
                } else {
                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}