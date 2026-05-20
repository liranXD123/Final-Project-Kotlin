package com.example.finalprojectweatherapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import com.example.finalprojectweatherapp.databinding.FragmentHomeBinding
import com.example.finalprojectweatherapp.utils.Resource
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequestToken: CancellationTokenSource? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            refreshWeather()
        } else {
            Toast.makeText(requireContext(), R.string.permission_rationale, Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupListeners()
        observeViewModel()

        if (hasLocationPermission()) {
            refreshWeather()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupListeners() {
        binding.btnRefresh.setOnClickListener {
            refreshWeather()
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

    private fun refreshWeather() {
        if (!hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        setRefreshInProgress(true)

        // Fast path: immediately re-fetch weather with the last known coordinates
        val usedCache = viewModel.refreshWithCachedLocation()

        // Also try to obtain a fresher device location (without blocking the fast path)
        requestCurrentLocation { lat, lon ->
            val latChanged = viewModel.lastLatitude?.let { kotlin.math.abs(it - lat) > 0.001 } ?: true
            val lonChanged = viewModel.lastLongitude?.let { kotlin.math.abs(it - lon) > 0.001 } ?: true
            if (!usedCache || latChanged || lonChanged) {
                viewModel.loadWeatherForLocation(lat, lon)
            }
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
                    onLocation(location.latitude, location.longitude)
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
                    onLocation(location.latitude, location.longitude)
                } else if (!viewModel.hasCachedLocation()) {
                    onLocationUnavailable()
                }
            }
            .addOnFailureListener {
                if (!viewModel.hasCachedLocation()) {
                    onLocationUnavailable()
                }
            }
    }

    private fun onLocationUnavailable() {
        setRefreshInProgress(false)
        Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT).show()
    }

    private fun setRefreshInProgress(inProgress: Boolean) {
        binding.layoutLoadingOverlay.isVisible = inProgress
        binding.layoutWeatherContent.isVisible = !inProgress
        binding.btnRefresh.isEnabled = !inProgress
    }

    private fun cancelLocationRequest() {
        locationRequestToken?.cancel()
        locationRequestToken = null
    }

    private fun observeViewModel() {
        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setRefreshInProgress(true)
                is Resource.Success -> {
                    setRefreshInProgress(false)
                    state.data?.let { data ->
                        binding.tvCityName.text = data.cityName
                        binding.tvTemperature.text = getString(R.string.temp_format, data.main.temperature.toInt())
                        binding.tvHumidity.text = getString(R.string.humidity_format, data.main.humidity)

                        WeatherIconLoader.load(
                            binding.ivWeatherIcon,
                            data.weatherConditions.firstOrNull()?.iconCode
                        )
                    }
                }
                is Resource.Error -> {
                    setRefreshInProgress(false)
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
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
