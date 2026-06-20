package com.example.finalprojectweatherapp.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Settings screen — app-wide preferences.
 * Dark mode via AppCompatDelegate; units and interval persist in SettingsManager (SharedPreferences).
 */
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    /**
     * User actions → ViewModel → SettingsManager (SharedPreferences + StateFlow).
     * Other screens (Favorites, Add Favorite) observe the same isCelsius StateFlow.
     */
    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.rgUnits.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setUnit(checkedId == R.id.rbCelsius)
        }

        binding.sliderInterval.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                viewModel.setUpdateInterval(value.toInt())
            }
        }
    }

    /** Syncs UI controls when settings load or change from ViewModel (e.g. after rotation). */
    private fun observeViewModel() {
        viewModel.isCelsius.observe(viewLifecycleOwner) { isCelsius ->
            if (isCelsius) {
                binding.rbCelsius.isChecked = true
            } else {
                binding.rbFahrenheit.isChecked = true
            }
        }

        viewModel.updateInterval.observe(viewLifecycleOwner) { interval ->
            binding.sliderInterval.value = interval.toFloat()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
