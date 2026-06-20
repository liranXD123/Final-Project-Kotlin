package com.example.finalprojectweatherapp.utils

/**
 * Formats temperatures for display in Favorites list and Add Favorite preview.
 *
 * API always returns Celsius (units=metric); conversion happens here for °F display only.
 */
object TemperatureUtils {

    fun toFahrenheit(celsius: Double): Double = (celsius * 9 / 5) + 32

    /**
     * @param temp Celsius from API or Room
     * @param isCelsius from SettingsManager
     * @return e.g. "25°C" or "77°F"
     */
    fun format(temp: Double, isCelsius: Boolean): String {
        val value = if (isCelsius) temp else toFahrenheit(temp)
        val unit = if (isCelsius) "C" else "F"
        return "${value.toInt()}°$unit"
    }
}
