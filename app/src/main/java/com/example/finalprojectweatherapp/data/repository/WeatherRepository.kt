package com.example.finalprojectweatherapp.data.repository

import com.example.finalprojectweatherapp.data.local.WeatherDao
import com.example.finalprojectweatherapp.data.local.WeatherEntity
import com.example.finalprojectweatherapp.data.remote.WeatherApi
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.remote.models.ForecastResponse
import com.example.finalprojectweatherapp.data.remote.models.PollutionResponse
import com.example.finalprojectweatherapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Hilt automatically injects the Api and Dao that we provided in AppModule.kt
class WeatherRepository @Inject constructor(
    val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
) {
    // --- LOCAL DATABASE OPERATIONS ---

    // Returns a Flow that the UI can observe for automatic updates
    fun getFavoritesFlow(): Flow<List<WeatherEntity>> = weatherDao.getFavorites()

    suspend fun addToFavorites(entity: WeatherEntity) = weatherDao.insertFavorite(entity)

    suspend fun removeFromFavorites(entity: WeatherEntity) = weatherDao.deleteFavorite(entity)

    suspend fun isFavorite(cityName: String): Boolean = weatherDao.isFavorite(cityName)

    // --- REMOTE NETWORK OPERATIONS ---

    suspend fun fetchCurrentWeather(lat: Double, lon: Double, apiKey: String): Resource<CurrentWeatherResponse> {
        return try {
            val response = weatherApi.getCurrentWeather(lat, lon, apiKey)
            if (response.isSuccessful) {
                response.body()?.let { resultResponse ->
                    return@let Resource.Success(resultResponse)
                } ?: Resource.Error("Received empty data from server")
            } else {
                Resource.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Couldn't reach the server. Please check your internet connection.")
        }
    }

    suspend fun fetchForecast(lat: Double, lon: Double, apiKey: String): Resource<ForecastResponse> {
        return try {
            val response = weatherApi.getForecast(lat, lon, apiKey)
            if (response.isSuccessful) {
                response.body()?.let { return@let Resource.Success(it) } ?: Resource.Error("Empty forecast data")
            } else {
                Resource.Error("Forecast error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error. Check connection.")
        }
    }

    suspend fun fetchAirPollution(lat: Double, lon: Double, apiKey: String): Resource<PollutionResponse> {
        return try {
            val response = weatherApi.getAirPollution(lat, lon, apiKey)
            if (response.isSuccessful) {
                response.body()?.let { return@let Resource.Success(it) } ?: Resource.Error("Empty pollution data")
            } else {
                Resource.Error("Pollution error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error. Check connection.")
        }
    }

    suspend fun fetchWeatherByCity(cityName: String, apiKey: String): Resource<CurrentWeatherResponse> {
        return try {
            val response = weatherApi.getWeatherByCity(cityName, apiKey)
            if (response.isSuccessful) {
                response.body()?.let { return@let Resource.Success(it) } ?: Resource.Error("Empty data")
            } else {
                Resource.Error("City not found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error. Check connection.")
        }
    }
}