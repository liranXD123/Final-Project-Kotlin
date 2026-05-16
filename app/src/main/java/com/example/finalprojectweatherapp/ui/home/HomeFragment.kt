package com.example.finalprojectweatherapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentHomeBinding
import com.example.finalprojectweatherapp.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        binding.btnViewLatest.setOnClickListener {
            findNavController().navigate(R.id.latestWeatherFragment)
        }

        checkLocationPermission()
        observeViewModel()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocationAndFetchWeather()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAndFetchWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModel.loadWeatherForLocation(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
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
        super.onDestroyView()
        _binding = null
    }
}