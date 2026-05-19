package com.example.finalprojectweatherapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentHomeBinding
import com.example.finalprojectweatherapp.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLocationAndFetchWeather()
        } else {
            Toast.makeText(requireContext(), R.string.permission_rationale, Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkLocationPermission()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRefresh.setOnClickListener {
            checkLocationPermission()
        }
        binding.btnViewPollution.setOnClickListener {
            val lat = viewModel.lastLatitude ?: 34.05
            val lon = viewModel.lastLongitude ?: -118.24
            findNavController().navigate(
                R.id.action_homeFragment_to_pollutionFragment,
                bundleOf("lat" to lat.toFloat(), "lon" to lon.toFloat())
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission() {
        if (hasLocationPermission()) {
            getLocationAndFetchWeather()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLocationAndFetchWeather() {
        if (!hasLocationPermission()) {
            checkLocationPermission()
            return
        }

        stopLocationUpdates()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                stopLocationUpdates()
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    viewModel.loadWeatherForLocation(lastLocation.latitude, lastLocation.longitude)
                } else {
                    Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT).show()
                }
            }
        }
        locationCallback = callback
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            if (::fusedLocationClient.isInitialized) {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
        locationCallback = null
    }

    private fun observeViewModel() {
        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBarHome.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBarHome.visibility = View.GONE
                    state.data?.let { data ->
                        binding.tvCityName.text = data.cityName
                        binding.tvTemperature.text = getString(R.string.temp_format, data.main.temperature.toInt())
                        binding.tvHumidity.text = getString(R.string.humidity_format, data.main.humidity)

                        val iconCode = data.weatherConditions.firstOrNull()?.iconCode
                        Glide.with(this)
                            .load("https://openweathermap.org/img/wn/${iconCode}@4x.png")
                            .into(binding.ivWeatherIcon)
                    }
                }
                is Resource.Error -> {
                    binding.progressBarHome.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        stopLocationUpdates()
        super.onDestroyView()
        _binding = null
    }
}
