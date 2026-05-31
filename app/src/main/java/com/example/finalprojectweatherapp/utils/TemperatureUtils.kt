package com.example.finalprojectweatherapp.utils

object TemperatureUtils {
    /**
     * Converts Celsius to Fahrenheit.
     */
    fun toFahrenheit(celsius: Double): Double = (celsius * 9 / 5) + 32

    /**
     * Formats temperature value according to user preference.
     * @param temp Temperature in Celsius (Metric)
     * @param isCelsius Whether to display in Celsius
     * @return Formatted string like "25°C" or "77°F"
     */
    fun format(temp: Double, isCelsius: Boolean): String {
        val value = if (isCelsius) temp else toFahrenheit(temp)
        val unit = if (isCelsius) "C" else "F"
        return "${value.toInt()}°$unit"
    }
}
