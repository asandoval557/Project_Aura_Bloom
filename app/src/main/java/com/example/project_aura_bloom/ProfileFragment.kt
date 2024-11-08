package com.example.project_aura_bloom

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.project_aura_bloom.databinding.FragmentProfileBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.charts.BarChart
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.core.models.Shape
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.*
import java.util.concurrent.TimeUnit
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FieldPath
import java.text.SimpleDateFormat

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var konfettiView: KonfettiView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data

        when (resultCode) {
            Activity.RESULT_OK -> {
                val fileUri = data?.data!!
                // Load the selected image into the profile image view
                Glide.with(this).load(fileUri).into(binding.profileImage)
                saveImageUriToFirestore(fileUri.toString())
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        konfettiView = binding.konfettiView

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Load User's profile
        loadUserProfile()

        // Click listener for profile picture changing
        binding.changePictureFab.setOnClickListener {
            ImagePicker.with(this)
                .crop()                                     // Crop the image if needed
                .compress(1024)                     // Final Image size will be less than 1 MB
                .maxResultSize(1080, 1080)      // Set the maximum resolution
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        // Click listener for edit button
        binding.editGeneralInfo.setOnClickListener {
            fetchProfileDataAndShowBottomSheet()
        }

        // Set up Meditation Bar Chart
        setupMeditationBarChart(binding.meditationBarChart)
    }

    private fun saveImageUriToFirestore(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId).get()
            .addOnSuccessListener {querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val documentId = querySnapshot.documents[0].id

                    // Update the "profileImageUrl" in the document
                    db.collection("AuraBloomUserData").document(documentId)
                        .update("profileImageUrl", imageUrl)
                        .addOnSuccessListener {
                            // Load image into profile image view
                            Glide.with(this).load(imageUrl).into(binding.profileImage)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Failed to upload image URL: ${exception.message}"
                                , Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to locate user document: ${exception.message}"
                    , Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        // Retrieve the document with the current's user's data
        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]

                    val name = document.getString("fullName") ?: ""
                    val dateOfBirth = document.getString("birthday") ?: ""
                    val dateJoined = document.getString("date_joined") ?: ""
                    val email = document.getString("email") ?: ""
                    val address = document.getString("address") ?: ""
                    val city = document.getString("city") ?: ""
                    val state = document.getString("state") ?: ""
                    val zipCode = document.getString("zipCode") ?: ""
                    val emergencyContactName = document.getString("emergencyContactName") ?: ""
                    val emergencyContactNumber = document.getString("emergencyContactPhoneNumber") ?: ""

                    // Retrieve the profile image URL
                    val profileImageUrl = document.getString("profileImageUrl")

                    // Calculate age
                    val age = calculateAgeFromDOB(dateOfBirth)

                    // Check if today is user's birthday
                    val isBirthday = checkIfTodayIsBirthday(dateOfBirth)
                    if (isBirthday) {
                        binding.birthdayMessage.visibility = View.VISIBLE
                        binding.birthdayMessage.text = "Happy Birthday $name!!"
                        startConfettiAnimation()
                    } else {
                        binding.birthdayMessage.visibility = View.GONE
                    }

                    // Update the UI elements with the retrieved data
                    binding.profileName.text = name
                    binding.profileDetails.text = "Age: $age\nJoined: $dateJoined"

                    // Load profile image if URL exists
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(binding.profileImage)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.ic_avatar)
                    }

                    // Populate General Information Panel
                    val cityStateZip = "$city, $state $zipCode"
                    val formattedGeneralInformation = formatGeneralInformation(
                        name = name,
                        email = email,
                        address = address,
                        cityStateZip = cityStateZip,
                        emergencyContactName = emergencyContactName,
                        emergencyContactPhoneNumber = emergencyContactNumber
                    )
                    binding.generalInformation.text = formattedGeneralInformation
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun formatGeneralInformation(name: String, email: String, address: String,
                                         cityStateZip: String, emergencyContactName: String, emergencyContactPhoneNumber: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()

        fun addBoldLabel(label: String, content: String) {
            val start = builder.length
            builder.append(label)
            builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(content).append("\n")
        }
        addBoldLabel("Name: ", name)
        addBoldLabel("Email: ", email)
        addBoldLabel("Address: ", "\n$address\n$cityStateZip")
        addBoldLabel("Emergency Contact: ", "\n$emergencyContactName\n${formatPhoneNumber(emergencyContactPhoneNumber)}")

        return builder
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length != 10) return phoneNumber
        return "(${phoneNumber.substring(0, 3)}) ${phoneNumber.substring(3,6)}-${phoneNumber.substring(6)}"
    }

    private fun startConfettiAnimation() {
        konfettiView.start(
            Party(
                speed = 10f,
                maxSpeed = 15f,
                damping =  0.9f,
                spread = 360,
                colors = listOf(Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.GREEN, Color.RED),
                position = Position.Relative(0.5, 0.3),
                size = listOf(Size.SMALL, Size.MEDIUM),
                timeToLive = 3000L,
                shapes = listOf(Shape.Circle, Shape.Square),
                emitter = Emitter(duration = 3, TimeUnit.SECONDS).max(100)
            )
        )
    }

    private fun checkIfTodayIsBirthday(bdayString: String): Boolean {
        if (bdayString.isEmpty()) return false

        val dateParts = bdayString.split("/")
        if (dateParts.size != 3) return false

        val month = dateParts[0].toIntOrNull() ?: return false
        val day = dateParts[1].toIntOrNull() ?: return false

        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)

        return month == currentMonth && day == currentDay
    }

    private fun calculateAgeFromDOB(dobString: String): Int {
        if (dobString.isEmpty()) return 0

        val dateParts = dobString.split("/")
        if (dateParts.size != 3) return 0 // Invalid date format

        val month = dateParts[0].toIntOrNull() ?: return 0
        val day = dateParts[1].toIntOrNull() ?: return 0
        val year = dateParts[2].toIntOrNull() ?: return 0

        val birthDate = Calendar.getInstance()
        birthDate.set(year, month - 1, day)

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun fetchProfileDataAndShowBottomSheet() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]

                    val name = document.getString("fullName") ?: ""
                    val dateOfBirth = document.getString("birthday") ?: ""
                    val email = document.getString("email") ?: ""
                    val address = document.getString("address") ?: ""
                    val city = document.getString("city") ?: ""
                    val state = document.getString("state") ?: ""
                    val zipCode = document.getString("zipCode") ?: ""
                    val emergencyContactName = document.getString("emergencyContactName") ?: ""
                    val emergencyContactPhone = document.getString("emergencyContactPhoneNumber") ?: ""

                    // Show the bottom sheet with fetched data
                    showEditProfileBottomSheet(
                        name,
                        dateOfBirth,
                        email,
                        address,
                        city,
                        state,
                        zipCode,
                        emergencyContactName,
                        emergencyContactPhone
                    )
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showEditProfileBottomSheet(
        name: String,
        dateOfBirth: String,
        email: String,
        address: String,
        city: String,
        state: String,
        zipCode: String,
        emergencyContactName: String,
        emergencyContactPhone: String
    ) {
        // Inflate the bottom sheet
        val bottomSheetView = layoutInflater.inflate(R.layout.edit_profile_bottom_sheet, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)

        // Reference the EditText fields in the bottom sheet
        val editName = bottomSheetDialog.findViewById<EditText>(R.id.edit_name)
        val datePicker = bottomSheetDialog.findViewById<DatePicker>(R.id.dob_datePicker)
        val editEmail = bottomSheetDialog.findViewById<EditText>(R.id.edit_email)
        val editAddress = bottomSheetDialog.findViewById<EditText>(R.id.edit_address)
        val editCity = bottomSheetDialog.findViewById<EditText>(R.id.edit_city)
        val stateSpinner = bottomSheetDialog.findViewById<Spinner>(R.id.state_spinner)
        val editZipCode = bottomSheetDialog.findViewById<EditText>(R.id.edit_zip_code)
        val editEmergencyName = bottomSheetDialog.findViewById<EditText>(R.id.edit_emergency_name)
        val editEmergencyPhone = bottomSheetDialog.findViewById<EditText>(R.id.edit_emergency_phone)

        // Pre-fill with data fetched from Firebase
        editName?.setText(name)
        editEmail?.setText(email)
        editAddress?.setText(address)
        editCity?.setText(city)
        editZipCode?.setText(zipCode)
        editEmergencyName?.setText(emergencyContactName)
        editEmergencyPhone?.setText(emergencyContactPhone)

        // Use DOB to initialize the DatePicker
        if (dateOfBirth.isNotEmpty()) {
            val dateParts = dateOfBirth.split("/")
            if (dateParts.size == 3) {
                val month = dateParts[0].toIntOrNull() ?: 1
                val day = dateParts[1].toIntOrNull() ?: 1
                val year = dateParts[2].toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                datePicker?.init(year, month - 1, day, null)
            }
        } else {
            // Set default date if DOB in not available
            val today = Calendar.getInstance()
            datePicker?.init(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),
                null
            )
        }

        // Initialize the Spinner with a hint
        stateSpinner?.let {
            setupStateSpinner(it, state)
        }

        // Handle Save click
        bottomSheetView.findViewById<TextView>(R.id.save_button).setOnClickListener {
            // Retrieve the updated information
            val updatedName = editName?.text.toString()
            val updatedEmail = editEmail?.text.toString()
            val updatedAddress = editAddress?.text.toString()
            val updatedCity = editCity?.text.toString()
            val selectedState = stateSpinner?.selectedItem.toString()
            val updatedZipCode = editZipCode?.text.toString()
            val updatedEmergencyName = editEmergencyName?.text.toString()
            val updatedEmergencyPhone = editEmergencyPhone?.text.toString()

            // Retrieve date from DatePicker
            val selectedMonth = datePicker?.month ?: Calendar.getInstance().get(Calendar.MONTH)
            val selectedDay = datePicker?.dayOfMonth ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val selectedYear = datePicker?.year ?: Calendar.getInstance().get(Calendar.YEAR)
            val updatedDateOfBirth = "${selectedMonth + 1}/$selectedDay/$selectedYear"

            // Check if the state is valid (not "State")
            if (selectedState == "State") {
                Toast.makeText(requireContext(), "Please select a valid state", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save or update the information in Firebase
            saveProfileInfo(
                updatedName,updatedDateOfBirth, updatedEmail, updatedAddress, updatedCity, selectedState,
                updatedZipCode, updatedEmergencyName, updatedEmergencyPhone
            )

            // Show a confirmation message and close bottom sheet
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        // Handle Cancel click
        bottomSheetDialog.findViewById<TextView>(R.id.cancel_button)?.setOnClickListener {
            // Close the bottom sheet without saving
            bottomSheetDialog.dismiss()
        }

        // Show bottom sheet dialog
        bottomSheetDialog.show()
    }

    private fun setupStateSpinner(stateSpinner: Spinner, selectedState: String) {
        // Get the list of states and add "State" as the first item
        val states = resources.getStringArray(R.array.us_states).toMutableList()
        states.add(0, "State")

        // Create adapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach the adapter to the Spinner
        stateSpinner.adapter = adapter

        // Set State as the default selection3
        if (selectedState.isNotEmpty() && selectedState != "State") {
            val statePosition = states.indexOf(selectedState)
            stateSpinner.setSelection(if (statePosition >= 0) statePosition else 0)
        } else {
            stateSpinner.setSelection(0)
        }
    }

    private fun saveProfileInfo(
        name: String, dateOfBirth: String, email: String, address: String,
        city: String, state: String, zipCode: String,
        emergencyContactName: String, emergencyContactPhone: String
    ) {
        val userId = auth.currentUser?.uid ?: return

        val userUpdates = mapOf(
            "fullName" to name,
            "birthday" to dateOfBirth,
            "email" to email,
            "address" to address,
            "city" to city,
            "state" to state,
            "zipCode" to zipCode,
            "emergencyContactName" to emergencyContactName,
            "emergencyContactPhoneNumber" to emergencyContactPhone
        )

        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val documentId = querySnapshot.documents[0].id

                    db.collection("AuraBloomUserData").document(documentId)
                        .set(userUpdates, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                requireContext(),
                                "Failed to update profile: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Document not found for current user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to locate document: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Function to set up Meditation Bar Chart
    private fun setupMeditationBarChart(barChart: BarChart) {
        val userId = auth.currentUser?.uid ?: return

        // Find the document ID for the current user based on their auth_uid
        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val userDocumentId = querySnapshot.documents[0].id

                    // Get the last 7 days in 'YYYY-MM-DD' format
                    val last7Days = getLast7Days()

                    // Query the dailySessions sub-collection for the last 7 days
                    db.collection("AuraBloomUserData")
                        .document(userDocumentId)
                        .collection("dailySessions")
                        .whereIn(FieldPath.documentId(), last7Days)
                        .get()
                        .addOnSuccessListener { dailySessionsSnapshot ->
                            val entries = mutableListOf<BarEntry>()
                            val dateToIndexMap = last7Days.withIndex().associate { it.value to it.index.toFloat() }

                            // Populate entries with data from Firestore
                            for (document in dailySessionsSnapshot.documents) {
                                val date = document.id
                                val durationInMinutes = document.getDouble("duration")?.toFloat() ?: 0f // Duration should be in minutes
                                val index = dateToIndexMap[date]
                                if (index != null) {
                                    entries.add(BarEntry(index, durationInMinutes))
                                }
                            }

                            // Fill missing days with zeroes if necessary
                            for (i in 0..6) {
                                if (entries.none { it.x == i.toFloat() }) {
                                    entries.add(BarEntry(i.toFloat(), 0f))
                                }
                            }

                            // Sort and set data for the BarChart
                            entries.sortBy { it.x }
                            val barDataSet = BarDataSet(entries, "Meditation Time (minutes)")
                            barDataSet.color = resources.getColor(R.color.purple, null)
                            val data = BarData(barDataSet)

                            barChart.data = data
                            barChart.invalidate() // Refresh the chart

                            // Customize the chart appearance
                            barChart.description.isEnabled = false
                            barChart.axisLeft.axisMinimum = 0f // Start y-axis at 0
                            barChart.axisLeft.axisMaximum = 30f // Stop y-axis at 60
                            barChart.xAxis.granularity = 1f
                            barChart.xAxis.labelCount = 7
                            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getLast7DaysLabels())
                            barChart.axisRight.isEnabled = false
                            barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            barChart.xAxis.setDrawGridLines(false)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Error loading meditation data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to locate user document: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getLast7Days(): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return List(7) {
            val date = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            date
        }.reversed() // Reversed to keep in chronological order
    }

    // Function to get last 7-day labels for X-axis
    private fun getLast7DaysLabels(): List<String> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Short day name
        val calendar = Calendar.getInstance()
        return List(7) {
            val dayLabel = dayFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            dayLabel
        }.reversed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}