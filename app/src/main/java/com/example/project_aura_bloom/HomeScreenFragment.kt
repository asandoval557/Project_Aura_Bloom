package com.example.project_aura_bloom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.project_aura_bloom.databinding.HomeScreenBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.project_aura_bloom.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeScreenFragment : Fragment() {

    private lateinit var binding: HomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var emergencyContactNumber: String? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Inflating the layout, and setting up the view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = HomeScreenBinding.inflate(inflater,container,false)
        val view = binding.root

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch Emergency Contact
        fetchEmergencyContact()
        // Check profile completion
        checkProfileCompletion()

        loadUserData()

        //  Initialize FusedLocationProviderClient for geolocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.geoLocation.setOnClickListener {
            if (checkLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }

        // Click listener for "Finish your profile"
        binding.finishYourProfile.setOnClickListener {
            findNavController().navigate(R.id.action_HomeScreenFragment_to_ProfileFragment)
        }

        // Click listener for the "Finish Your Drawing" panel
        binding.finishDrawingPanel.setOnClickListener {
            findNavController().navigate(R.id.action_HomeScreenFragment_to_DrawingFragment)
        }

        // Click listener for the "How are you today?" button
        binding.btnHowAreYou.setOnClickListener {
            findNavController().navigate(R.id.action_HomeScreenFragment_to_MoodProgressFragment)
        }

        // Help Button functionality with Bottom Sheet
        binding.btnHelp.setOnClickListener {
            showHelpOptions()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return // Make sure the user is logged in

        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId)
            .get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val userDataDocument = querySnapshot.documents[0]
                    val userProfile = userDataDocument.toObject(UserProfile::class.java)
                    userProfile?.let {updateUI(it)}

                    // Load profile image if URL exists
                    val profileImageUrl = userDataDocument.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrBlank()) {
                        Glide.with(this).load(profileImageUrl).into(binding.profileImage)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.ic_avatar)
                    }
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load user data ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(userProfile: UserProfile) {
        binding.tvName.text = userProfile.fullName
        binding.tvJoined.text = "Joined ${userProfile.date_joined}"

        // Format emergency contact phone number
        val formattedPhoneNumber = formatPhoneNumber(userProfile.emergencyContactPhoneNumber)
        binding.tvContact.text = "Emergency Contact: ${userProfile.emergencyContactName}\n$formattedPhoneNumber"

        // Load the profile image
        if (!userProfile.profileImageUrl.isNullOrBlank()) {
            Glide.with(this).load(userProfile.profileImageUrl).into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_avatar)
        }
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        if (phoneNumber == null || phoneNumber.length != 10) return phoneNumber ?: ""
        return "(${phoneNumber.substring(0, 3)}) ${phoneNumber.substring(3,6)}-${phoneNumber.substring(6)}"
    }

    private fun checkProfileCompletion() {
        val userId = auth.currentUser!!.uid ?: return
        //updating the logic to search for the firebase user ID for the current user
        db.collection("AuraBloomUserData")
            //search based on the auth_id field within the AuraBloomUserData documents
            .whereEqualTo("auth_uid",userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot != null && !querySnapshot.isEmpty) {
                    val userDataDocument = querySnapshot.documents[0]
                    var filledFields = 0
                    val totalFields = 5 // Adjust based on # of field in collection

                    // Check each field (Change based on Field names)
                    // If "name" field is NOT empty or NULL, increment filledFields
                    if (!userDataDocument.getString("fullName").isNullOrEmpty()) filledFields++
                    if (!userDataDocument.getString("birthday").isNullOrEmpty()) filledFields++
                    if (!userDataDocument.getString("email").isNullOrEmpty()) filledFields++
                    if (!userDataDocument.getString("emergencyContactName").isNullOrEmpty()) filledFields++
                    if (!userDataDocument.getString("emergencyContactPhoneNumber").isNullOrEmpty()) filledFields++

                    // Calculate profile completion percentage
                    val completionPercentage = (filledFields / totalFields.toDouble() * 100).toInt()

                    // Update the ProgressBar and TextView
                    updateProfileCompletionUI(completionPercentage)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileCompletionUI(percentage: Int) {
        if (percentage < 100) {
            binding.profileCompletionLayout.visibility = View.VISIBLE
            binding.profileCompletionBar.progress = percentage
            binding.profileCompletionText.text = "$percentage%"
        } else {
            binding.profileCompletionLayout.visibility = View.GONE
        }
    }

    //TODO: Adrian, this is where the Firebase pull for Emergency Contact happens
    private fun fetchEmergencyContact() {
        // Retrieve the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        // Query the Firestore database for user data associated with the current user's ID
        db.collection("AuraBloomUserData")
            // Filter the documents where the 'auth_uid' field matches the current user's ID
            .whereEqualTo("auth_uid", userId)
            .get() // Execute the query
            .addOnSuccessListener { querySnapshot ->
                // Check if any documents were returned in the query result
                if (!querySnapshot.isEmpty) {
                    // Get the first matching document from the query result
                    val document = querySnapshot.documents[0]
                    // Retrieve the emergency contact phone number from the document
                    emergencyContactNumber = document.getString("emergencyContactPhoneNumber")

                } else {
                    // Show a toast message if no documents were found
                    Toast.makeText(requireContext(), "No emergency contact found", Toast.LENGTH_SHORT).show()
                }
            }
            // Handle any errors that occur during the query
            .addOnFailureListener { exception ->
                // Show a toast message with the error details
                Toast.makeText(requireContext(), "Error fetching contact: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showHelpOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.help_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        // Suicide and Crisis Lifeline
        view.findViewById<Button>(R.id.crisisLifelineButton).setOnClickListener {
            dialPhone("988")
            bottomSheetDialog.dismiss()
        }

        // Call 911
        view.findViewById<Button>(R.id.emergencyButton).setOnClickListener {
            dialPhone("911")
            bottomSheetDialog.dismiss()
        }

        // Emergency Contact
        view.findViewById<Button>(R.id.emergencyContactButton).setOnClickListener {
            if(emergencyContactNumber != null) {
                dialPhone(emergencyContactNumber!!)
            } else {
                Toast.makeText(requireContext(),"No Emergency Contact available",Toast.LENGTH_SHORT).show()
            }
            bottomSheetDialog.dismiss()
        }

        // Crisis Text Line
        view.findViewById<Button>(R.id.crisisTextLineButton).setOnClickListener {
            sendTextMessage("838255", "Hello")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun sendTextMessage(number: String, message: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("smsto:$number")
            putExtra("sms_body", message)
        }
        startActivity(intent)
    }

    // Check if location permissions are granted
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

    // Request location permissions
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    // Function to retrieve the current location
    private fun getCurrentLocation() {
        // Check if permission is granted before getting location
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {

            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(requireContext(),"Latitude : $latitude, Longitude : $longitude",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(),"Location not available",Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception: Exception ->
                Toast.makeText(requireContext(),"Failed to get location: ${exception.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun dialPhone(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") }
        startActivity(intent)
    }
}