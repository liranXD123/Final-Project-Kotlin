package com.example.finalprojectweatherapp.utils

import androidx.annotation.StringRes
import com.example.finalprojectweatherapp.R

data class PollutantInfo(
    val key: String,
    @StringRes val nameRes: Int,
    val displayMax: Double
)

enum class PollutantSeverity(@StringRes val labelRes: Int) {
    LOW(R.string.pollutant_level_low),
    MEDIUM(R.string.pollutant_level_medium),
    HIGH(R.string.pollutant_level_high)
}

object PollutantDisplay {

    val trackedPollutants = listOf(
        PollutantInfo("pm2_5", R.string.pollutant_pm25, 75.0),
        PollutantInfo("pm10", R.string.pollutant_pm10, 150.0),
        PollutantInfo("no2", R.string.pollutant_no2, 200.0),
        PollutantInfo("o3", R.string.pollutant_o3, 180.0),
        PollutantInfo("co", R.string.pollutant_co, 5000.0)
    )

    fun severity(value: Double, displayMax: Double): PollutantSeverity {
        val ratio = value / displayMax
        return when {
            ratio < 0.35 -> PollutantSeverity.LOW
            ratio < 0.7 -> PollutantSeverity.MEDIUM
            else -> PollutantSeverity.HIGH
        }
    }

    fun progressPercent(value: Double, displayMax: Double): Int {
        return ((value / displayMax) * 100).toInt().coerceIn(0, 100)
    }
}
