package com.example.finalprojectweatherapp.ui.addfavorite

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentAddFavoriteBinding
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

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

    private fun observeViewModel() {
        viewModel.searchResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.pbSearch.visibility = View.VISIBLE
                    binding.btnSaveFavorite.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.pbSearch.visibility = View.GONE
                    binding.tvSearchResult.text = "${state.data?.cityName}: ${state.data?.main?.temperature}°C"
                    binding.btnSaveFavorite.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.pbSearch.visibility = View.GONE
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