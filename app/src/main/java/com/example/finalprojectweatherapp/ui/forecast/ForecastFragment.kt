package com.example.finalprojectweatherapp.ui.forecast

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentForecastBinding
import com.example.finalprojectweatherapp.utils.Resource
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForecastFragment : Fragment(R.layout.fragment_forecast) {

    private val viewModel: ForecastViewModel by viewModels()
    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!
    private lateinit var forecastAdapter: ForecastAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequestToken: CancellationTokenSource? = null
    private lateinit var geocoder: Geocoder
    private var currentLocationName: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadForecastForCurrentLocation()
        } else {
            Toast.makeText(requireContext(), R.string.permission_rationale, Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForecastBinding.bind(view)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireContext())

        setupRecyclerView()
        observeViewModel()

        if (hasLocationPermission()) {
            loadForecastForCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadForecastForCurrentLocation() {
        if (!hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        requestCurrentLocation { lat, lon ->
            viewModel.loadForecast(lat, lon)
        }
    }

    private fun requestCurrentLocation(onLocation: (lat: Double, lon: Double) -> Unit) {
        cancelLocationRequest()

        val tokenSource = CancellationTokenSource()
        locationRequestToken = tokenSource

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(60_000)
            .build()

        fusedLocationClient.getCurrentLocation(request, tokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    getLocationNameAndUpdate(location.latitude, location.longitude, onLocation)
                } else {
                    tryLastKnownLocation(onLocation)
                }
            }
            .addOnFailureListener {
                tryLastKnownLocation(onLocation)
            }
    }

    private fun tryLastKnownLocation(onLocation: (lat: Double, lon: Double) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    getLocationNameAndUpdate(location.latitude, location.longitude, onLocation)
                } else {
                    onLocationUnavailable()
                }
            }
            .addOnFailureListener {
                onLocationUnavailable()
            }
    }

    private fun getLocationNameAndUpdate(
        lat: Double,
        lon: Double,
        onLocation: (lat: Double, lon: Double) -> Unit
    ) {
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                var locationName = address.locality ?: address.adminArea ?: "Unknown Location"
                // Normalize location name by removing common suffixes (e.g., "Tel Aviv-Yafo" -> "Tel Aviv")
                locationName = locationName.split("-")[0].trim()
                currentLocationName = locationName
            } else {
                currentLocationName = "Current Location"
            }
        } catch (e: Exception) {
            currentLocationName = "Current Location"
        }
        updateTitleWithLocation()
        onLocation(lat, lon)
    }

    private fun updateTitleWithLocation() {
        if (_binding != null) {
            binding.tvForecastTitle.text = "Weekly Outlook ($currentLocationName)"
        }
    }

    private fun onLocationUnavailable() {
        Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT).show()
    }

    private fun cancelLocationRequest() {
        locationRequestToken?.cancel()
        locationRequestToken = null
    }

    private fun setupRecyclerView() {
        forecastAdapter = ForecastAdapter()
        binding.rvForecast.apply {
            adapter = forecastAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.isCelsius.observe(viewLifecycleOwner) { isCelsius ->
            forecastAdapter.isCelsius = isCelsius
            forecastAdapter.notifyDataSetChanged()
        }

        viewModel.forecastState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is Resource.Loading -> {
                    binding.progressBarForecast.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBarForecast.visibility = View.GONE
                    forecastAdapter.submitList(state.data)
                }
                is Resource.Error -> {
                    binding.progressBarForecast.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        cancelLocationRequest()
        super.onDestroyView()
        _binding = null
    }
}