// package declaration for the utility classes
package com.example.finalprojectweatherapp.utils

// import the string resource annotation from androidx
import androidx.annotation.StringRes
// import the local project resource class
import com.example.finalprojectweatherapp.R

// define a data class to hold configuration information for a specific pollutant
data class PollutantInfo(
    // the string key used by the api to identify the pollutant
    val key: String,
    // the string resource id for the localized display name of the pollutant
    @StringRes val nameRes: Int,
    // the maximum expected concentration value used to calculate severity and progress scales
    val displayMax: Double
)

// define an enum class to represent the health risk severity levels of a pollutant
enum class PollutantSeverity(
    // the string resource id representing the localized label for the severity level
    @StringRes val labelRes: Int
) {
    // low severity level indicating good air quality for the pollutant
    LOW(R.string.pollutant_level_low),
    // medium severity level indicating moderate air quality for the pollutant
    MEDIUM(R.string.pollutant_level_medium),
    // high severity level indicating poor air quality for the pollutant
    HIGH(R.string.pollutant_level_high)
}

// declare a singleton object to handle pollutant display logic and configuration
object PollutantDisplay {

    // define a list of predefined pollutants that the app tracks and displays
    val trackedPollutants = listOf(
        // configuration for fine particulate matter (pm2.5)
        PollutantInfo("pm2_5", R.string.pollutant_pm25, 75.0),
        // configuration for coarse particulate matter (pm10)
        PollutantInfo("pm10", R.string.pollutant_pm10, 150.0),
        // configuration for nitrogen dioxide (no2)
        PollutantInfo("no2", R.string.pollutant_no2, 200.0),
        // configuration for ozone (o3)
        PollutantInfo("o3", R.string.pollutant_o3, 180.0),
        // configuration for carbon monoxide (co)
        PollutantInfo("co", R.string.pollutant_co, 5000.0)
    )

    /*
     * calculates the severity level of a pollutant based on its current value and display maximum.
     * * parameters:
     * value - the current concentration value of the pollutant
     * displaymax - the maximum threshold value used to gauge severity
     * * returns: the computed pollutantseverity enum value (low, medium, or high)
     */
    fun severity(value: Double, displayMax: Double): PollutantSeverity {
        // calculate the ratio of the current value against the maximum display value
        val ratio = value / displayMax
        // evaluate the calculated ratio to determine severity tier
        return when {
            // return low severity if the ratio is under 35 percent
            ratio < 0.35 -> PollutantSeverity.LOW
            // return medium severity if the ratio is under 70 percent but above 35 percent
            ratio < 0.7 -> PollutantSeverity.MEDIUM
            // return high severity for anything at or above 70 percent
            else -> PollutantSeverity.HIGH
        }
    }

    /*
     * calculates the percentage value for rendering a progress bar, constrained between 0 and 100.
     * * parameters:
     * value - the current concentration value of the pollutant
     * displaymax - the maximum threshold value representing 100 percent
     * * returns: an integer representing the percentage progress to display
     */
    fun progressPercent(value: Double, displayMax: Double): Int {
        // calculate the percentage, convert to integer, and clamp the value between 0 and 100
        return ((value / displayMax) * 100).toInt().coerceIn(0, 100)
    }
}