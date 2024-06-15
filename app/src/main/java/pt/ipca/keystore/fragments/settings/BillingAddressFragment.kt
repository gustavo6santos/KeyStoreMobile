package pt.ipca.keystore.fragments.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import pt.ipca.keystore.R
import pt.ipca.keystore.data.api.GeocodeResponse
import pt.ipca.keystore.databinding.FragmentBillingAddressBinding
import pt.ipca.keystore.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BillingAddressFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var _binding: FragmentBillingAddressBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val PERMISSIONS_REQUEST_LOCATION = 101

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

        RetrofitClient.service.geocode(address, "d5476f2cc9d049c7a7e5513b886410ae").enqueue(object : Callback<GeocodeResponse> {
            override fun onResponse(call: Call<GeocodeResponse>, response: Response<GeocodeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val locations = response.body()?.results
                    if (!locations.isNullOrEmpty()) {
                        val firstLocation = locations.first().geometry
                        updateMap(firstLocation.lat, firstLocation.lng)
                    } else {
                        Toast.makeText(context, "No results found", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<GeocodeResponse>, t: Throwable) {
                Toast.makeText(context, "Failure: ${t.message}", Toast.LENGTH_LONG).show()
                Log.d("API", "Failure: ${t.message}")
            }
        })
    }

    private fun updateMap(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(location).title("Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
