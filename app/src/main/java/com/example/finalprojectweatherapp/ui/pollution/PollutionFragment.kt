package com.example.finalprojectweatherapp.ui.pollution

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentPollutionBinding
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PollutionFragment : Fragment(R.layout.fragment_pollution) {

    private val viewModel: PollutionViewModel by viewModels()
    private var _binding: FragmentPollutionBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPollutionBinding.bind(view)

        observeViewModel()

        // Fetch pollution for a sample location
        viewModel.getPollutionData(34.05, -118.24)
    }

    private fun observeViewModel() {
        viewModel.pollutionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.pbPollution.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.pbPollution.visibility = View.GONE
                    state.data?.list?.firstOrNull()?.let { data ->
                        binding.tvAqi.text = getString(R.string.aqi_format, data.main.aqi)
                        binding.tvCo.text = "CO: ${data.components["co"]}"
                        binding.tvNo2.text = "NO2: ${data.components["no2"]}"
                    }
                }
                is Resource.Error -> {
                    binding.pbPollution.visibility = View.GONE
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