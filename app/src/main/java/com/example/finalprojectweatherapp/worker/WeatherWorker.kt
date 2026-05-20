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

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WeatherRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val favorites = repository.getFavoritesFlow().first()
            for (favorite in favorites) {
                val result = repository.fetchWeatherByCity(favorite.cityName, Constants.API_KEY)
                if (result is Resource.Success && result.data != null) {
                    val updatedEntity = WeatherEntity(
                        cityName = result.data.cityName,
                        temperature = result.data.main.temperature,
                        description = result.data.weatherConditions.firstOrNull()?.description ?: "",
                        iconUrl = WeatherIconLoader.iconUrl(
                            result.data.weatherConditions.firstOrNull()?.iconCode
                        )
                    )
                    repository.addToFavorites(updatedEntity)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}