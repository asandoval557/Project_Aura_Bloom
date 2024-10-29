package com.example.project_aura_bloom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.project_aura_bloom.databinding.HomeScreenBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeScreenFragment : Fragment() {

    private lateinit var binding: HomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var db: FirebaseFirestore
    private var emergencyContactNumber: String? = null

    // Inflating the layout, and setting up the view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = HomeScreenBinding.inflate(inflater,container,false)
        val view = binding.root

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Fetch Emergency Contact
        fetchEmergencyContact()

        // Click listener for the "Finish Your Drawing" panel
        binding.finishDrawingPanel.setOnClickListener {
            findNavController().navigate(R.id.action_HomeScreenFragment_to_DrawingFragment)
        }

        return view
    }

    //TODO: Adrian, this is where the Firebase pull for Emergency Contact happens
    private fun fetchEmergencyContact() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    emergencyContactNumber = document.getString("contact_number")
                } else {
                    Toast.makeText(requireContext(),"No contact found",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener{exception ->
                Toast.makeText(requireContext(),"Error fetching contact: ${exception.message}",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  Initialize FusedLocationProviderClient for geolocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.geoLocation.setOnClickListener {
            if (checkLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }

        // Help Button functionality with Bottom Sheet
        binding.btnHelp.setOnClickListener {
            showHelpOptions()
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