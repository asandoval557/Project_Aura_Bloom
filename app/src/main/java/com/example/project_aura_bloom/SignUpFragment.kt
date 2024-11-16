package com.example.project_aura_bloom

import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
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
import java.text.SimpleDateFormat
import java.util.Locale


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

        // Handle password toggle
        var isPasswordVisible = false
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Show password
                binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivPasswordToggle.setImageResource(R.drawable.hide) // Show Closed eye
            } else {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.view) // Show Open eye
            }
            // Keep cursor at end of input
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        // Handle confirm password toggle
        var isConfirmPasswordVisible = false
        binding.ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                // Show confirm password
                binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivConfirmPasswordToggle.setImageResource(R.drawable.hide) // Show Closed eye
            } else {
                binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivConfirmPasswordToggle.setImageResource(R.drawable.view) // Show Open eye
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }

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

            //Add valid email check
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if Terms and Conditions are checked
            if (!binding.cbTerms.isChecked) {
                Toast.makeText(context, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Create the User Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //User has been created successfully
                    val userID = auth.currentUser?.uid
                    //Move user information to FireStore
                    storeUserInfo(userID, fullName, email)
                    }
                else
                {
                    //Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SignUpFragment", "User creation failed: ${task.exception?.message}")
                }
            }
        }

        // Handle "Log In" text click to navigate to LogInFragment
        binding.tvLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_LoginFragment)
        }

        // Handle "Read Terms and Conditions" text click to navigate to TermsFragment
        binding.tvTerms.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_TermsFragment)
        }
    }
    //
    private fun storeUserInfo( userID: String?, fullName: String, email: String) {
        // Reference to the document that stores the current user count
            val auraUserDF = signUpDB.collection("auraUserID").document("auraUserCount")
        // Retrieve the authenticated user's ID, exit function if null
            val authUid = auth.currentUser?.uid ?: return

        // Get current date in "Month-Year" format for the date_joined field
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val dateJoined = dateFormat.format(System.currentTimeMillis())

        // Fetch the most recent userID and increment it
            auraUserDF.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Get current user count, default to 1 if null
                        val currentUserID = document.getLong("UserID") ?: 1
                        val newUserID = currentUserID + 1

                        // Format the new user ID to a 6-digit string (e.g., "000001")
                        val idFormat = String.format("%06d", newUserID)

                        // Create a map of user data to be stored
                        val auraUser = hashMapOf(
                            "auth_uid" to authUid,
                            "fullName" to fullName,
                            "email" to email,
                            "user_id" to idFormat,
                            "date_joined" to dateJoined
                        )
                        // Add the new user data to the "AuraBloomUserData" collection
                        signUpDB.collection("AuraBloomUserData")
                            .add(auraUser)
                            .addOnSuccessListener {
                                Log.d("SignUpFragment", "User information successfully updated")
                                // Update the user count in auraUserID collection
                                auraUserDF.update("UserID", newUserID)
                                    .addOnSuccessListener {
                                        Log.d("SignUpFragment", "User count incremented successfully")
                                        // Navigate to the Login Fragment after successful registration
                                        findNavController().navigate(R.id.action_SignUpFragment_to_LoginFragment)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("SignUpFragment", "Error updating User count: ", e)
                                        Toast.makeText(context, "Failed to update user count", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.w("SignUpFragment", "Error updating User info: ", e)
                                Toast.makeText(context, "Failed to store user info", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.w("SignUpFragment", "UserCounter document does not exist.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("SignUpFragment", "Error fetching latestUserID: ", e)
                }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}