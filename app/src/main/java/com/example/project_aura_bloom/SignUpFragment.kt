package com.example.project_aura_bloom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_aura_bloom.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    //Implement firebase authentication and database variables
    private lateinit var auth: FirebaseAuth
    private lateinit var signUpDB: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //implement the authorization and creating the database for userSignUpDB
        auth = Firebase.auth
        signUpDB = FirebaseFirestore.getInstance()

        //Handle Sign Up button click
        binding.btnSignUp.setOnClickListener {
            //SignUp Variables and get user input
            val fullName = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            //Validate User Input
            if(fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Confirm Password Match
            if(password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Create the User Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //User has been created successfully
                    val userID = auth.currentUser ?.uid
                    //Move user information to FireStore
                    storeUserInfo(userID, fullName, email)
                    }
                else
                {
                    Toast.makeText(context, "Authentication failed: ${task.exception}.message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle "Log In" text click to navigate to LogInFragment
        binding.tvLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_LoginFragment)
        }
    }
    //
    private fun storeUserInfo( userID: String?, fullName: String, email: String)
    {
    if(userID != null)
        {
            val user = hashMapOf("fullName" to fullName, "email" to email)

            //Store the user information in the "UserSignUp" collection
            signUpDB.collection("UserSignUp").document(userID.toString()).set(user).addOnSuccessListener {
                Log.d("SignUpFragment", "User info successfully updated")
                //TODO Do we want to update the screen to reflect that the user can now sign in or when the user is successfully created take them to the homepage?
            }
                .addOnFailureListener{ e -> Log.w("SignUpFragment", "Error updating User info: ", e)
                }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}