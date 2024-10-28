package com.example.project_aura_bloom

import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.project_aura_bloom.databinding.HomeScreenBinding

class HomeScreenFragment : Fragment() {

    private lateinit var binding: HomeScreenBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Inflating the layout, and setting up the view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = HomeScreenBinding.inflate(inflater,container,false)
        val view = binding.root

        // Click listener for the "Finish Your Drawing" panel
        binding.finishDrawingPanel.setOnClickListener {
            findNavController().navigate(R.id.action_HomeScreenFragment_to_DrawingFragment)
        }

        return view
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
}