package com.example.finalprojectweatherapp.data.repository

import com.example.finalprojectweatherapp.data.local.ForecastEntity
import com.example.finalprojectweatherapp.data.local.WeatherDao
import com.example.finalprojectweatherapp.data.local.WeatherEntity
import com.example.finalprojectweatherapp.data.local.forecastLocationKey
import com.example.finalprojectweatherapp.data.local.toForecastEntity
import com.example.finalprojectweatherapp.data.remote.WeatherApi
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.remote.models.ForecastResponse
import com.example.finalprojectweatherapp.data.remote.models.PollutionResponse
import com.example.finalprojectweatherapp.utils.LanguageUtils
import com.example.finalprojectweatherapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Hilt injects WeatherApi + WeatherDao from AppModule. ViewModels only talk to this class.
class WeatherRepository @Inject constructor(
    val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
) {
    // --- FAVORITES — Room CRUD + city search API ---

    /** Pass-through to DAO Flow — FavoritesViewModel collects for reactive UI. */
    fun getFavoritesFlow(): Flow<List<WeatherEntity>> = weatherDao.getFavorites()

    suspend fun addToFavorites(entity: WeatherEntity) = weatherDao.insertFavorite(entity)

    suspend fun removeFromFavorites(entity: WeatherEntity) = weatherDao.deleteFavorite(entity)

    suspend fun isFavoriteById(cityId: Int): Boolean = weatherDao.isFavoriteById(cityId)

    // --- FORECAST CACHE (observe Room, refresh from API) ---

    fun observeForecast(lat: Double, lon: Double): Flow<List<ForecastEntity>> {
        val locationKey = forecastLocationKey(lat, lon)
        return weatherDao.observeForecast(locationKey)
    }

    suspend fun getCachedForecast(lat: Double, lon: Double): List<ForecastEntity> {
        return weatherDao.getForecastSnapshot(forecastLocationKey(lat, lon))
    }

    /**
     * Fetches forecast from Retrofit, saves to Room, and returns any network error.
     * UI should observe [observeForecast] for displayed data.
     */
    suspend fun refreshForecast(lat: Double, lon: Double, apiKey: String): Resource<Unit> {
        val locationKey = forecastLocationKey(lat, lon)
        return when (val result = fetchForecast(lat, lon, apiKey)) {
            is Resource.Success -> {
                val items = result.data?.forecastList?.map { it.toForecastEntity(locationKey) } ?: emptyList()
                if (items.isEmpty()) {
                    Resource.Error("Empty forecast data")
                } else {
                    weatherDao.replaceForecastForLocation(locationKey, items)
                    Resource.Success(Unit)
                }
            }
            is Resource.Error -> Resource.Error(result.message ?: "Forecast error")
            is Resource.Loading -> Resource.Error("Unexpected loading state")
        }
    }

    // --- REMOTE NETWORK OPERATIONS ---

    suspend fun fetchCurrentWeather(lat: Double, lon: Double, apiKey: String): Resource<CurrentWeatherResponse> {
        return try {
            val response = weatherApi.getCurrentWeather(lat, lon, apiKey, "metric", LanguageUtils.getSystemLanguage())
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
            val response = weatherApi.getForecast(lat, lon, apiKey, "metric", LanguageUtils.getSystemLanguage())
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

    /**
     * Add Favorite search: GET weather?q=cityName.
     * Wraps HTTP result in Resource so ViewModel/Fragment can show loading/error without crashes.
     */
    suspend fun fetchWeatherByCity(cityName: String, apiKey: String): Resource<CurrentWeatherResponse> {
        return try {
            val response = weatherApi.getWeatherByCity(cityName, apiKey, "metric", LanguageUtils.getSystemLanguage())
            if (response.isSuccessful) {
                response.body()?.let { return@let Resource.Success(it) } ?: Resource.Error("Empty data")
            } else {
                Resource.Error("City not found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error. Check connection.")
        }
    }

    /**
     * Refresh favorite rows after language/unit change.
     * Uses city ID so the correct city is fetched regardless of stored name language.
     */
    suspend fun fetchWeatherById(cityId: Int, apiKey: String): Resource<CurrentWeatherResponse> {
        return try {
            val response = weatherApi.getWeatherById(cityId, apiKey, "metric", LanguageUtils.getSystemLanguage())
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