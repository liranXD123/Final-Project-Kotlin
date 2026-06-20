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
import com.example.finalprojectweatherapp.utils.TemperatureUtils
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
        _binding = FragmentHomeBinding.bind(view) // bind the xml
        // init location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // init button click listeners
        setupListeners()
        // start observing the live data model
        observeViewModel()

        // instantly check for location permission
        if (hasLocationPermission()) {
            refreshWeather()
        } else {
            // if we dont have permission request it from user
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupListeners() {
        // bind refresh button to refresh weather func
        binding.btnRefresh.setOnClickListener {
            refreshWeather()
        }
        // bind air pollution details button
        binding.btnViewPollution.setOnClickListener {
            // use the saved coordinates or default if none saved
            val lat = viewModel.lastLatitude ?: 34.05
            val lon = viewModel.lastLongitude ?: -118.24
            // navigate to the pollution fragment with the coordinates
            findNavController().navigate(
                R.id.action_homeFragment_to_pollutionFragment,
                bundleOf("lat" to lat.toFloat(), "lon" to lon.toFloat())
            )
        }
    }

    // helper to make sure we have location permission
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun refreshWeather() {
        if (!hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            // if we dont have permission we don't refresh weather
            return
        }
        setRefreshInProgress(true)

        // if we already have location we do quick refresh
        val usedCache = viewModel.refreshWithCachedLocation()

        // sync func to get device location
        requestCurrentLocation { lat, lon ->
            val latChanged = viewModel.lastLatitude?.let { kotlin.math.abs(it - lat) > 0.001 } ?: true
            val lonChanged = viewModel.lastLongitude?.let { kotlin.math.abs(it - lon) > 0.001 } ?: true
            // if we didnt have cache or location changed we refresh
            if (!usedCache || latChanged || lonChanged) {
                viewModel.loadWeatherForLocation(lat, lon)
            }
        }
    }

    private fun requestCurrentLocation(onLocation: (lat: Double, lon: Double) -> Unit) {
        // stop any existing loc request
        cancelLocationRequest()

        // init cencellation token
        val tokenSource = CancellationTokenSource()
        locationRequestToken = tokenSource

        // config for the req
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(60_000)
            .build()

        // get current location
        fusedLocationClient.getCurrentLocation(request, tokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocation(location.latitude, location.longitude)
                } else {
                    // fallback if phone location is unavailable
                    tryLastKnownLocation(onLocation)
                }
            }
            .addOnFailureListener {
                // fallback if the request fails
                tryLastKnownLocation(onLocation)
            }
    }

    private fun tryLastKnownLocation(onLocation: (lat: Double, lon: Double) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // send coordinates back through the callback
                    onLocation(location.latitude, location.longitude)
                } else if (!viewModel.hasCachedLocation()) {
                    onLocationUnavailable()
                }
            }
            .addOnFailureListener {
                if (!viewModel.hasCachedLocation()) {
                    // notify users if we failed and we have no cache
                    onLocationUnavailable()
                }
            }
    }

    // renders a toast with location unavailble error
    private fun onLocationUnavailable() {
        setRefreshInProgress(false)
        Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT).show()
    }

    // toggle loading state on the UI
    private fun setRefreshInProgress(inProgress: Boolean) {
        binding.layoutLoadingOverlay.isVisible = inProgress
        binding.layoutWeatherContent.isVisible = !inProgress
        binding.btnRefresh.isEnabled = !inProgress
    }

    // utility to cancel a location request
    private fun cancelLocationRequest() {
        locationRequestToken?.cancel()
        locationRequestToken = null
    }

    // init observers to update UI elements
    private fun observeViewModel() {
        // observer for (Celsius/Fahrenheit)
        viewModel.isCelsius.observe(viewLifecycleOwner) {
            // if weather data is loaded update the UI
            viewModel.weatherState.value?.let { state ->
                if (state is Resource.Success) {
                    updateWeatherUI(state.data!!)
                }
            }
        }

        // observer for weather data
        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setRefreshInProgress(true)
                is Resource.Success -> {
                    setRefreshInProgress(false)
                    state.data?.let { data ->
                        updateWeatherUI(data)
                    }
                }
                // if an error occurs show a toast
                is Resource.Error -> {
                    setRefreshInProgress(false)
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // update ui components with weather data
    private fun updateWeatherUI(data: com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse) {
        // get current unit from settings
        val isCelsius = viewModel.isCelsius.value ?: true
        // set city name
        binding.tvCityName.text = data.cityName
        // format and set temperature
        binding.tvTemperature.text = TemperatureUtils.format(data.main.temperature, isCelsius)
        // set humidity
        binding.tvHumidity.text = getString(R.string.humidity_format, data.main.humidity)

        // load weather icon (optimized with glide utility)
        WeatherIconLoader.load(
            binding.ivWeatherIcon,
            data.weatherConditions.firstOrNull()?.iconCode
        )
    }

    // cleanup when view is destroyed
    override fun onDestroyView() {
        cancelLocationRequest()
        super.onDestroyView()
        _binding = null
    }
}
