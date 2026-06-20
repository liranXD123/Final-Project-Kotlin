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
        _binding = FragmentHomeBinding.bind(view)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize button click listeners
        setupListeners()
        // Start observing LiveData from the ViewModel
        observeViewModel()

        // Check for location permissions immediately
        if (hasLocationPermission()) {
            // Fetch weather data if permission exists
            refreshWeather()
        } else {
            // Request location permission from the user
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Configures the click listeners for UI interactive elements.
     */
    private fun setupListeners() {
        // Set click listener for the refresh button
        binding.btnRefresh.setOnClickListener {
            // Trigger a manual weather update
            refreshWeather()
        }
        // Set click listener for the air pollution details button
        binding.btnViewPollution.setOnClickListener {
            // Use last known latitude or a default fallback
            val lat = viewModel.lastLatitude ?: 34.05
            // Use last known longitude or a default fallback
            val lon = viewModel.lastLongitude ?: -118.24
            // Navigate to the PollutionFragment passing coordinates as arguments
            findNavController().navigate(
                R.id.action_homeFragment_to_pollutionFragment,
                bundleOf("lat" to lat.toFloat(), "lon" to lon.toFloat())
            )
        }
    }

    /**
     * Verifies if the ACCESS_FINE_LOCATION permission is granted.
     * @return True if permission is granted, false otherwise.
     */
    private fun hasLocationPermission(): Boolean {
        // Check current permission status via ContextCompat
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Main logic for refreshing weather data.
     * Handles permissions, caching, and fresh location requests.
     */
    private fun refreshWeather() {
        // Verify location permissions before proceeding
        if (!hasLocationPermission()) {
            // Launch permission request if missing
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            // Exit early if permission is not granted
            return
        }

        // Display the loading overlay to the user
        setRefreshInProgress(true)

        // Attempt a fast refresh using coordinates already stored in memory
        val usedCache = viewModel.refreshWithCachedLocation()

        // Fetch the current device location asynchronously
        requestCurrentLocation { lat, lon ->
            // Determine if the latitude has changed significantly
            val latChanged = viewModel.lastLatitude?.let { kotlin.math.abs(it - lat) > 0.001 } ?: true
            // Determine if the longitude has changed significantly
            val lonChanged = viewModel.lastLongitude?.let { kotlin.math.abs(it - lon) > 0.001 } ?: true
            // If cache wasn't used or location is different, fetch new weather
            if (!usedCache || latChanged || lonChanged) {
                // Request new weather data from the network
                viewModel.loadWeatherForLocation(lat, lon)
            }
        }
    }

    /**
     * Requests the current GPS location from the FusedLocationProviderClient.
     * @param onLocation Lambda callback function invoked with latitude and longitude.
     */
    private fun requestCurrentLocation(onLocation: (lat: Double, lon: Double) -> Unit) {
        // Stop any currently running location requests
        cancelLocationRequest()

        // Create a new source for the cancellation token
        val tokenSource = CancellationTokenSource()
        // Save the source to the property for later cancellation
        locationRequestToken = tokenSource

        // Configure the location request settings
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(60_000)
            .build()

        // Call Google Play Services to get the current location
        fusedLocationClient.getCurrentLocation(request, tokenSource.token)
            .addOnSuccessListener { location ->
                // If a valid location was returned
                if (location != null) {
                    // Send coordinates back through the callback
                    onLocation(location.latitude, location.longitude)
                } else {
                    // Try to get the last known location as a fallback
                    tryLastKnownLocation(onLocation)
                }
            }
            .addOnFailureListener {
                // Try fallback location if the request fails
                tryLastKnownLocation(onLocation)
            }
    }

    /**
     * Attempts to retrieve the last recorded location on the device.
     * @param onLocation Lambda callback function invoked with latitude and longitude.
     */
    private fun tryLastKnownLocation(onLocation: (lat: Double, lon: Double) -> Unit) {
        // Access the last known location from the client
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // If a last known location exists
                if (location != null) {
                    // Send coordinates back through the callback
                    onLocation(location.latitude, location.longitude)
                } else if (!viewModel.hasCachedLocation()) {
                    // Handle failure if no location is available anywhere
                    onLocationUnavailable()
                }
            }
            .addOnFailureListener {
                // If failure and no coordinates cached, notify the user
                if (!viewModel.hasCachedLocation()) {
                    // Show error UI for unavailable location
                    onLocationUnavailable()
                }
            }
    }

    /**
     * Displays a toast and resets UI state when location cannot be determined.
     */
    private fun onLocationUnavailable() {
        // Turn off the loading indicator
        setRefreshInProgress(false)
        // Inform the user that location services failed
        Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT).show()
    }

    /**
     * Switches the UI between loading state and content state.
     * @param inProgress If true, shows loading; if false, shows weather data.
     */
    private fun setRefreshInProgress(inProgress: Boolean) {
        // Set visibility of the loading overlay layout
        binding.layoutLoadingOverlay.isVisible = inProgress
        // Set visibility of the weather data container
        binding.layoutWeatherContent.isVisible = !inProgress
        // Disable refresh button while loading to prevent multiple triggers
        binding.btnRefresh.isEnabled = !inProgress
    }

    /**
     * Cancels the active location request token.
     */
    private fun cancelLocationRequest() {
        // Call cancel on the token source if it exists
        locationRequestToken?.cancel()
        // Nullify the token reference
        locationRequestToken = null
    }

    /**
     * Initializes the LiveData observers to update UI on data changes.
     */
    private fun observeViewModel() {
        // Observe changes to the temperature unit setting (Celsius/Fahrenheit)
        viewModel.isCelsius.observe(viewLifecycleOwner) {
            // If weather data is already loaded
            viewModel.weatherState.value?.let { state ->
                // Ensure the state is successful before updating
                if (state is Resource.Success) {
                    // Re-render the UI with the updated unit
                    updateWeatherUI(state.data!!)
                }
            }
        }

        // Observe the main weather data state from the repository
        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when (state) {
                // When data is being fetched
                is Resource.Loading -> setRefreshInProgress(true)
                // When data is successfully retrieved
                is Resource.Success -> {
                    // Hide the loading spinner
                    setRefreshInProgress(false)
                    // Extract data and update views
                    state.data?.let { data ->
                        // Populate UI with weather info
                        updateWeatherUI(data)
                    }
                }
                // When an error occurs during fetch
                is Resource.Error -> {
                    // Hide the loading spinner
                    setRefreshInProgress(false)
                    // Show error message via toast
                    Toast.makeText(requireContext(), state.message ?: getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Updates individual UI components with data from a CurrentWeatherResponse.
     * @param data The weather response data object.
     */
    private fun updateWeatherUI(data: com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse) {
        // Determine the current temperature unit from the ViewModel
        val isCelsius = viewModel.isCelsius.value ?: true
        // Set the city name text view
        binding.tvCityName.text = data.cityName
        // Format the temperature and set it to the text view
        binding.tvTemperature.text = TemperatureUtils.format(data.main.temperature, isCelsius)
        // Set the humidity percentage text view
        binding.tvHumidity.text = getString(R.string.humidity_format, data.main.humidity)

        // Use the Glide utility to load the weather icon from the web
        WeatherIconLoader.load(
            binding.ivWeatherIcon,
            data.weatherConditions.firstOrNull()?.iconCode
        )
    }

    /**
     * Standard Fragment cleanup called when the view is destroyed.
     */
    override fun onDestroyView() {
        // Clean up pending location requests
        cancelLocationRequest()
        // Call super method for standard cleanup
        super.onDestroyView()
        // Prevent memory leaks by clearing the binding reference
        _binding = null
    }
}
