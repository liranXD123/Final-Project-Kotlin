package com.example.finalprojectweatherapp.ui.latest

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentLatestWeatherBinding
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LatestWeatherFragment : Fragment(R.layout.fragment_latest_weather) {

    private val viewModel: LatestWeatherViewModel by viewModels()
    private var _binding: FragmentLatestWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var latestAdapter: LatestWeatherAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLatestWeatherBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        viewModel.startUpdates(15) // Minimum periodic interval is 15 minutes
    }

    private fun setupRecyclerView() {
        latestAdapter = LatestWeatherAdapter()
        binding.rvLatest.apply {
            adapter = latestAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.latestWeather.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.pbLatest.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.pbLatest.visibility = View.GONE
                    latestAdapter.submitList(state.data)
                }
                is Resource.Error -> {
                    binding.pbLatest.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}