package com.example.project_aura_bloom

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.project_aura_bloom.models.Quote


class HomeScreenFragment : Fragment() {

    private lateinit var binding: HomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 1001
    private var emergencyContactNumber: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    private var currentStreak: Long = 0
    private var totalSessions: Long = 0
    private lateinit var milestones: List<Long>

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Inflating the layout, and setting up the view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = HomeScreenBinding.inflate(inflater,container,false)
        val view = binding.root

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("QuotePreferences", Context.MODE_PRIVATE)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addMilestoneFieldsToExistingUsers() // Call to update fields for the current users

        val userId = auth.currentUser!!.uid
        trackLoginStreak(userId)

        initializeAchievementsIfNeeded(userId)
        loadAchievements(userId)

        // Fetch Emergency Contact
        fetchEmergencyContact()
        // Check profile completion
        checkProfileCompletion()
        // Loads User's information
        loadUserData()
        // Profile Picture flip animation
        flipProfilePicture()

        // Load and display the Quote of the Day
        displayQuoteOfTheDay()

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

        // Click listener for the "How are you today?" button
        binding.btnHowAreYou.setOnClickListener {
            findNavController().navigate(R.id.action_HomeScreenFragment_to_MoodProgressFragment)
        }

        // Help Button functionality with Bottom Sheet
        binding.btnHelp.setOnClickListener {
            showHelpOptions()
        }

        // Click listener for profile image
        binding.profileImage.setOnClickListener {
            flipProfilePicture()
        }
    }

    private fun flipProfilePicture() {
        val profileImage = binding.profileImage // Front-side picture
        val animatedImage = binding.animatedProfileImage // Back-side animated picture

        // Animate to rotate from 0 to 90 degrees
        val flipOut = ObjectAnimator.ofFloat(profileImage, "rotationY", 0f, 90f)
        flipOut.duration = 300
        // Animate to rotate from 90 to 180 degrees
        val flipIn = ObjectAnimator.ofFloat(animatedImage, "rotationY", -90f, 0f)
        flipIn.duration = 300

        // Listener to switch images at the halfway point
        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                profileImage.visibility = View.GONE
                animatedImage.visibility = View.VISIBLE
                // Load GIF
                Glide.with(this@HomeScreenFragment).asGif().load(R.drawable.malcolm_gif).into(animatedImage)
                flipIn.start()
            }
        })
        // Start the 1st animation
        flipOut.start()
        // Return to original profile picture after a delay
        animatedImage.postDelayed({
            // Reverse the animation
            val reverseFlipOut = ObjectAnimator.ofFloat(animatedImage, "rotationY", 0f, 90f)
            val reverseFlipIn = ObjectAnimator.ofFloat(profileImage, "rotationY", -90f, 0f)

            reverseFlipOut.duration = 300
            reverseFlipIn.duration = 300

            reverseFlipOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animatedImage.visibility = View.GONE
                    profileImage.visibility = View.VISIBLE
                    reverseFlipIn.start()
                }
            })
            reverseFlipOut.start()
        }, 4000) // Delay in milliseconds before flipping back
    }

    private fun trackLoginStreak(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId)
            .get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val docRef = db.collection("AuraBloomUserData").document(document.id)
                    val lastLoginDate = document.getTimestamp("lastLoginDate")?.toDate()
                    val currentDate = Date()
                    val calendar = Calendar.getInstance()
                    calendar.time = currentDate
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val todayDate = calendar.time

                    var updatedStreak = document.getLong("currentStreak") ?: 0

                    if (lastLoginDate != null) {
                        val differenceInDays = TimeUnit.MILLISECONDS.toDays(todayDate.time - lastLoginDate.time).toInt()

                        if (differenceInDays == 1) {
                            // Increment streak if it's the next consecutive day
                            updatedStreak++
                        } else if (differenceInDays > 1) {
                            // Reset streak if more than one day has passed
                            updatedStreak = 1
                        }
                    } else {
                        // Set streak to 1 if no previous login date exists
                        updatedStreak = 1
                    }

                    docRef.update(
                        mapOf(
                            "currentStreak" to updatedStreak,
                            "lastLoginDate" to todayDate
                        )
                    ).addOnSuccessListener {
                        updateAchievementUI(updatedStreak)
                    }.addOnFailureListener { exception ->
                        println("Error updating streak: ${exception.message}")
                    }
                } else {
                    println("No document found for userId: $userId")
                }
            }.addOnFailureListener { exception ->
                println("Error getting documents: ${exception.message}")
            }
    }

    private fun displayQuoteOfTheDay() {
        // Get the last update time
        val lastUpdateTime = sharedPreferences.getLong("lastUpdateTime", 0)
        val currentTime = System.currentTimeMillis()

        //Check if 24 hours has passed since last update
        if (currentTime - lastUpdateTime >= TimeUnit.HOURS.toMillis(24)) {
            // 24 hours has passed
            fetchQuoteFromFirestore()
        } else {
            // Less than 24 hours, load saved quote
            val savedQuote = sharedPreferences.getString("quoteText", "No Quote Available")
            val savedAuthor = sharedPreferences.getString("quoteAuthor", "Author Unknown")
            binding.QuoteOfTheDay.text = "\"$savedQuote\" - $savedAuthor"
        }
    }

    private fun fetchQuoteFromFirestore() {
        db.collection("Quotes").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Get random document (quote) from the List of Quotes
                val quotesList = querySnapshot.documents
                val randomQuoteDoc = quotesList.random()

                val quoteText = randomQuoteDoc.getString("text") ?: "No Quote Available"
                val quoteAuthor = randomQuoteDoc.getString("author") ?: "Author Unknown"

                // Display the quote in the TextView
                binding.QuoteOfTheDay.text = "\"$quoteText\" - $quoteAuthor"

                // Save the quote in SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("quoteText", quoteText)
                    putString("quoteAuthor", quoteAuthor)
                    putLong("lastUpdateTime", System.currentTimeMillis())
                    apply()
                }

                // Fade-in animation
                val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                binding.QuoteOfTheDay.startAnimation(fadeInAnimation)
            } else {
                binding.QuoteOfTheDay.text = "No Quote found for today"
            }
        }.addOnFailureListener { exception ->
            binding.QuoteOfTheDay.text = "Error loading quote"
            println("Error getting quote: ${exception.message}")
        }
    }

    private fun initializeAchievementsIfNeeded(userId: String) {
        val userDocRef = db.collection("AuraBloomUserData").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = mutableMapOf<String, Any>()

                if (!document.contains("currentStreak")) data["currentStreak"] = 0
                if (!document.contains("totalSessions")) data["totalSessions"] = 0
                if (!document.contains("milestones")) data["milestones"] = listOf(5, 10, 20)

                if (data.isNotEmpty()) {
                    userDocRef.update(data)
                }
            }
        }
    }

    private fun loadAchievements(userId: String) {
        val userDocRef = db.collection("AuraBloomUserData").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                currentStreak = document.getLong("currentStreak") ?: 0
                totalSessions = document.getLong("totalSessions") ?: 0
                milestones = (document["milestones"] as? List<*>)?.filterIsInstance<Long>() ?: listOf(5, 10, 20, 50, 100)

                updateAchievementUI(currentStreak)
            }
        }
    }

    private fun updateAchievementUI(currentStreak: Long) {
        // Define milestones
        val milestones = listOf(5, 10, 20, 50, 100)
        val nextMilestone = (milestones.firstOrNull { it > currentStreak } ?: (currentStreak + 10)).toLong()
        val daysUntilNextMilestone = nextMilestone - currentStreak

        // Update current achievement panel
        binding.currentAchievementCount.text = currentStreak.toString()
        binding.currentAchievementLabel.text = if (currentStreak == 1L) "DAY" else "DAYS"
        binding.currentAchievementMessage.text = "Way to go!\nKeep it going!"

        // Update next milestone panel
        binding.nextMilestoneCount.text = nextMilestone.toString()
        binding.nextMilestoneLabel.text = if (nextMilestone == 1L) "DAY" else "DAYS"
        binding.nextMilestoneMessage.text = "You\'re only $daysUntilNextMilestone days away!"
    }

    private fun addMilestoneFieldsToExistingUsers() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId)
            .get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val docRef = db.collection("AuraBloomUserData").document(document.id)
                    val updates = hashMapOf(
                        "currentStreak" to (document.getLong("currentStreak") ?: 0),
                        "totalSessions" to (document.getLong("totalSessions") ?: 0),
                        "milestones" to listOf(5, 10, 20, 50, 100),
                        "achievements" to mapOf(
                            "5_sessions" to false,
                            "10_sessions" to false,
                            "20_sessions" to false,
                            "50_sessions" to false,
                            "100_sessions" to false
                        )
                    )
                    docRef.update(updates).addOnSuccessListener {
                        println("Updated document ${document.id} successfully.")
                    }.addOnFailureListener { exception ->
                        println("Error updating document ${document.id} failed with ${exception.message}")
                    }
                }
            }.addOnFailureListener { exception ->
                println("Error retrieving documents: ${exception.message}")
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
        if (userProfile.profileImageUrl.isNotBlank()) {
            Glide.with(this).load(userProfile.profileImageUrl).into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_avatar)
        }
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length != 10) return phoneNumber
        return "(${phoneNumber.substring(0, 3)}) ${phoneNumber.substring(3,6)}-${phoneNumber.substring(6)}"
    }

    private fun checkProfileCompletion() {
        val userId = auth.currentUser!!.uid
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
            locationPermissionRequestCode)
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

    override fun onResume() {
        super.onResume()

        flipProfilePicture()
    }
}