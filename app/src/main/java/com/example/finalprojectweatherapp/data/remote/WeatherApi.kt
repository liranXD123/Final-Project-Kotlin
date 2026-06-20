package com.example.finalprojectweatherapp.data.remote

import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.remote.models.ForecastResponse
import com.example.finalprojectweatherapp.data.remote.models.PollutionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for OpenWeatherMap.
 *
 * suspend functions run on background threads; Gson maps JSON to Kotlin data classes.
 */
interface WeatherApi {

    // Query 1: Current weather by GPS (Home)
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): Response<CurrentWeatherResponse>

    // Query 2: 5-Day Forecast
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): Response<ForecastResponse>

    // Query 3: Air Pollution
    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<PollutionResponse>

    /**
     * Add Favorite: search by city name before saving to Room.
     * Same /weather endpoint, different query param (q vs lat/lon).
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): Response<CurrentWeatherResponse>

    /**
     * Refresh stored favorite when language changes.
     * ID is language-independent (unlike city name string).
     */
    @GET("data/2.5/weather")
    suspend fun getWeatherById(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): Response<CurrentWeatherResponse>
}
