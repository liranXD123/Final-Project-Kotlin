package com.example.finalprojectweatherapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finalprojectweatherapp.data.local.WeatherEntity
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Background worker managed by WorkManager.
 * Wakes up periodically to fetch the latest weather for all saved favorite cities
 * and securely updates the local Room database, ensuring data is fresh even if the app is closed.
 */
@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WeatherRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Take a single snapshot of the database using .first()
            val favorites = repository.getFavoritesFlow().first()

            for (favorite in favorites) {
                // Fetching by unique ID instead of city name prevents errors with identical city names globally
                val result = repository.fetchWeatherById(favorite.id, Constants.API_KEY)
                if (result is Resource.Success && result.data != null) {
                    val updatedEntity = WeatherEntity(
                        id = result.data.id,
                        cityName = result.data.cityName,
                        temperature = result.data.main.temperature,
                        description = result.data.weatherConditions.firstOrNull()?.description ?: "",
                        iconUrl = WeatherIconLoader.iconUrl(
                            result.data.weatherConditions.firstOrNull()?.iconCode
                        )
                    )
                    // @Insert(REPLACE) inside the DAO handles the update seamlessly
                    repository.addToFavorites(updatedEntity)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}