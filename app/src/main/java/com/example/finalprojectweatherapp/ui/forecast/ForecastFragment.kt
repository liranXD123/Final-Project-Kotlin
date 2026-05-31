package com.example.finalprojectweatherapp.ui.forecast

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentForecastBinding
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForecastFragment : Fragment(R.layout.fragment_forecast) {

    private val viewModel: ForecastViewModel by viewModels()
    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForecastBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        // For demo purposes, fetching for a default location if no args
        viewModel.loadForecast(34.05, -118.24) // Los Angeles
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
        super.onDestroyView()
        _binding = null
    }
}