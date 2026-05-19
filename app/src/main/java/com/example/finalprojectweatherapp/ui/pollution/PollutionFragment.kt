package com.example.finalprojectweatherapp.ui.pollution

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.data.remote.models.PollutionItem
import com.example.finalprojectweatherapp.databinding.FragmentPollutionBinding
import com.example.finalprojectweatherapp.databinding.ItemPollutantRowBinding
import com.example.finalprojectweatherapp.utils.AqiLevel
import com.example.finalprojectweatherapp.utils.PollutantDisplay
import com.example.finalprojectweatherapp.utils.PollutantSeverity
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PollutionFragment : Fragment(R.layout.fragment_pollution) {

    private val viewModel: PollutionViewModel by viewModels()
    private var _binding: FragmentPollutionBinding? = null
    private val binding get() = _binding!!

    private val scaleSegments by lazy {
        listOf(
            binding.scaleSegment1,
            binding.scaleSegment2,
            binding.scaleSegment3,
            binding.scaleSegment4,
            binding.scaleSegment5
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPollutionBinding.bind(view)

        observeViewModel()

        val lat = arguments?.getFloat("lat", 34.05f)?.toDouble() ?: 34.05
        val lon = arguments?.getFloat("lon", -118.24f)?.toDouble() ?: -118.24
        viewModel.getPollutionData(lat, lon)
    }

    private fun observeViewModel() {
        viewModel.pollutionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    state.data?.list?.firstOrNull()?.let { item ->
                        showContent(item)
                    } ?: showError(getString(R.string.error_unknown))
                }
                is Resource.Error -> showError(state.message ?: getString(R.string.error_network))
            }
        }
    }

    private fun showLoading() {
        binding.pbPollution.isVisible = true
        binding.contentPollution.isVisible = false
        binding.cardPollutionError.isVisible = false
    }

    private fun showError(message: String) {
        binding.pbPollution.isVisible = false
        binding.contentPollution.isVisible = false
        binding.cardPollutionError.isVisible = true
        binding.tvPollutionError.text = message
    }

    private fun showContent(item: PollutionItem) {
        binding.pbPollution.isVisible = false
        binding.cardPollutionError.isVisible = false
        binding.contentPollution.isVisible = true

        val level = AqiLevel.fromIndex(item.main.aqi)
        bindAqiHero(level)
        bindPollutantRows(item.components)
    }

    private fun bindAqiHero(level: AqiLevel) {
        val context = requireContext()
        val color = ContextCompat.getColor(context, level.colorRes)
        val backgroundColor = ContextCompat.getColor(context, level.backgroundRes)

        binding.cardAqiHero.setCardBackgroundColor(backgroundColor)
        binding.ivAqiIcon.setImageResource(level.iconRes)
        binding.tvAqiLevelLabel.text = getString(level.labelRes)
        binding.tvAqiLevelLabel.setTextColor(color)
        binding.tvAqiIndex.text = getString(R.string.aqi_index_format, level.index)
        binding.tvAqiDescription.text = getString(level.descriptionRes)
        binding.tvHealthAdvice.text = getString(level.adviceRes)

        updateAqiScale(level)
    }

    private fun updateAqiScale(activeLevel: AqiLevel) {
        val allLevels = AqiLevel.entries
        scaleSegments.forEachIndexed { index, segment ->
            val level = allLevels[index]
            val isActive = level.index <= activeLevel.index
            val colorRes = if (isActive) level.colorRes else R.color.aqi_scale_inactive
            val drawable = GradientDrawable().apply {
                cornerRadius = 12f * resources.displayMetrics.density
                setColor(ContextCompat.getColor(requireContext(), colorRes))
            }
            segment.background = drawable
            segment.alpha = if (level == activeLevel) 1f else if (isActive) 0.85f else 0.4f
        }
    }

    private fun bindPollutantRows(components: Map<String, Double>) {
        binding.containerPollutants.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        PollutantDisplay.trackedPollutants.forEach { pollutant ->
            val value = components[pollutant.key] ?: return@forEach
            val rowBinding = ItemPollutantRowBinding.inflate(inflater, binding.containerPollutants, false)
            bindPollutantRow(rowBinding, pollutant.nameRes, value, pollutant.displayMax)
            binding.containerPollutants.addView(rowBinding.root)
        }

        if (binding.containerPollutants.childCount == 0) {
            val emptyView = TextView(requireContext()).apply {
                text = getString(R.string.pollution_no_components)
                setPadding(0, 16, 0, 16)
            }
            binding.containerPollutants.addView(emptyView)
        }
    }

    private fun bindPollutantRow(
        rowBinding: ItemPollutantRowBinding,
        nameRes: Int,
        value: Double,
        displayMax: Double
    ) {
        val severity = PollutantDisplay.severity(value, displayMax)
        val progress = PollutantDisplay.progressPercent(value, displayMax)
        val colorRes = when (severity) {
            PollutantSeverity.LOW -> R.color.pollutant_low
            PollutantSeverity.MEDIUM -> R.color.pollutant_medium
            PollutantSeverity.HIGH -> R.color.pollutant_high
        }
        val color = ContextCompat.getColor(requireContext(), colorRes)

        rowBinding.tvPollutantName.setText(nameRes)
        rowBinding.tvPollutantValue.text = getString(R.string.pollutant_value_format, value)
        rowBinding.tvPollutantLevel.text = getString(severity.labelRes)
        rowBinding.tvPollutantLevel.setTextColor(color)
        rowBinding.progressPollutant.progress = progress
        rowBinding.progressPollutant.setIndicatorColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
