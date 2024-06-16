package pt.ipca.keystore.fragments.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pt.ipca.keystore.R
import pt.ipca.keystore.databinding.FragmentBillingAddressBinding
import pt.ipca.keystore.util.Resource
import pt.ipca.keystore.viewmodel.ProfileViewModel
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class BillingAddressFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var _binding: FragmentBillingAddressBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val PERMISSIONS_REQUEST_LOCATION = 101

    // Inject the ProfileViewModel
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillingAddressBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupLocationClient()

        binding.buttonGeocode.setOnClickListener {
            geocodeAddress()
        }

        binding.buttonUseCurrentLocation.setOnClickListener {
            checkLocationSettingsAndStartLocationUpdates()
        }

        binding.saveAddress.setOnClickListener {
            saveAddress()
        }

        // Observe the address data and populate the EditText fields
        observeAddress()
        observeUpdateAddressResult()
    }

    private fun setupLocationClient() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.locations[0]
                    updateMap(location.latitude, location.longitude)
                    getAddressFromCoordinates(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this) // Stop further updates
                }
            }
        }
    }

    private fun checkLocationSettingsAndStartLocationUpdates() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are satisfied, start location updates
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION)
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Location not enabled", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun geocodeAddress() {
        val street = binding.editTextStreet.text.toString()
        val number = binding.editTextNumber.text.toString()
        val city = binding.editTextCity.text.toString()
        val country = binding.editTextCountry.text.toString()
        val address = "$street $number, $city, $country"

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>?

        try {
            addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                updateMap(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "No results found", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Geocoding failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateMap(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(location).title("Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun getAddressFromCoordinates(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare ?: ""
                val number = address.subThoroughfare ?: ""
                val city = address.locality ?: ""
                val country = address.countryName ?: ""

                binding.editTextStreet.setText(street)
                binding.editTextNumber.setText(number)
                binding.editTextCity.setText(city)
                binding.editTextCountry.setText(country)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun observeAddress() {
        lifecycleScope.launchWhenStarted {
            viewModel.address.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        val address = resource.data
                        if (address != null) {
                            binding.editTextStreet.setText(address.street)
                            binding.editTextNumber.setText(address.number)
                            binding.editTextCity.setText(address.city)
                            binding.editTextCountry.setText(address.country)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeUpdateAddressResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.updateAddressResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success -> {
                        Toast.makeText(context, "Address updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveAddress() {
        val street = binding.editTextStreet.text.toString()
        val number = binding.editTextNumber.text.toString()
        val city = binding.editTextCity.text.toString()
        val country = binding.editTextCountry.text.toString()

        if (street.isNotEmpty() && number.isNotEmpty() && city.isNotEmpty() && country.isNotEmpty()) {
            viewModel.updateAddress(street, number, city, country)
        } else {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
