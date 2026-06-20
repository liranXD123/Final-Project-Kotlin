package com.example.finalprojectweatherapp.ui.addfavorite

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentAddFavoriteBinding
import com.example.finalprojectweatherapp.utils.Resource
import com.example.finalprojectweatherapp.utils.TemperatureUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Add Favorite screen — search city online, preview, save to Room.
 * Demonstrates Retrofit (search) and Room (save) in one user flow.
 */
@AndroidEntryPoint
class AddFavoriteFragment : Fragment(R.layout.fragment_add_favorite) {

    private val viewModel: AddFavoriteViewModel by viewModels()
    private var _binding: FragmentAddFavoriteBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddFavoriteBinding.bind(view)

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchCity.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchCity(query)
            }
        }

        binding.btnSaveFavorite.setOnClickListener {
            val result = viewModel.searchResult.value
            if (result is Resource.Success && result.data != null) {
                viewModel.saveToFavorites(result.data)
                Toast.makeText(requireContext(), "Saved to favorites!", Toast.LENGTH_SHORT).show()
            }
        }

        observeViewModel()
    }

    /**
     * Reacts to Resource states from the ViewModel — standard loading/success/error UI pattern.
     * Also re-formats temperature when user changes °C/°F in Settings while on this screen.
     */
    private fun observeViewModel() {
        viewModel.isCelsius.observe(viewLifecycleOwner) {
            viewModel.searchResult.value?.let { state ->
                if (state is Resource.Success) {
                    updateSearchUI(state.data!!)
                }
            }
        }

        viewModel.searchResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.pbSearch.visibility = View.VISIBLE
                    binding.cvResult.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.pbSearch.visibility = View.GONE
                    binding.cvResult.visibility = View.VISIBLE
                    updateSearchUI(state.data!!)
                }
                is Resource.Error -> {
                    binding.pbSearch.visibility = View.GONE
                    binding.cvResult.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Shows preview text before save — uses same TemperatureUtils as the favorites list. */
    private fun updateSearchUI(data: com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse) {
        val isCelsius = viewModel.isCelsius.value ?: true
        val tempFormatted = TemperatureUtils.format(data.main.temperature, isCelsius)
        binding.tvSearchResult.text = "${data.cityName}: $tempFormatted"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
