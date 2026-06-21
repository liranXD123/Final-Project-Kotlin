package com.example.finalprojectweatherapp.data.local

import com.example.finalprojectweatherapp.data.remote.models.ForecastItem
import com.example.finalprojectweatherapp.data.remote.models.MainStats
import com.example.finalprojectweatherapp.data.remote.models.WeatherCondition

/**
 * Maps a complex Remote API model (ForecastItem) to a flat Room entity (ForecastEntity).
 * Discards unnecessary API data to save RAM and device storage.
 */
fun ForecastItem.toForecastEntity(locationKey: String): ForecastEntity {
    val weather = weatherConditions.firstOrNull()
    return ForecastEntity(
        id = "${locationKey}_$dateTime",
        locationKey = locationKey,
        dateTime = dateTime,
        temperature = main.temperature,
        iconCode = weather?.iconCode ?: "01d",
        description = weather?.description ?: ""
    )
}

/**
 * Maps a Room entity back to the UI model expected by the RecyclerView adapter.
 */
fun ForecastEntity.toForecastItem(): ForecastItem {
    return ForecastItem(
        dateTime = dateTime,
        main = MainStats(
            temperature = temperature,
            feelsLike = temperature,
            humidity = 0
        ),
        weatherConditions = listOf(
            WeatherCondition(
                condition = "",
                description = description,
                iconCode = iconCode
            )
        )
    )
}

fun forecastLocationKey(lat: Double, lon: Double): String {
    return "%.2f_%.2f".format(lat, lon)
}