package com.example.finalprojectweatherapp.ui.latest

import androidx.lifecycle.asLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.LanguageUtils
import com.example.finalprojectweatherapp.utils.Resource
import com.example.finalprojectweatherapp.utils.SettingsManager
import com.example.finalprojectweatherapp.worker.WeatherWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LatestWeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val workManager: WorkManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _latestWeather = MutableLiveData<Resource<List<CurrentWeatherResponse>>>()
    val latestWeather: LiveData<Resource<List<CurrentWeatherResponse>>> = _latestWeather

    val isCelsius = settingsManager.isCelsius.asLiveData()

    /**
     * Returns a localized list of cities based on system language.
     * Tokyo will now be "טוקיו" in Hebrew and "Tokyo" in English.
     */
    private fun getLocalizedCities(): List<String> {
        return if (LanguageUtils.getSystemLanguage() == "he") {
            listOf("לונדון", "ניו יורק", "טוקיו", "פריז", "ברלין")
        } else {
            listOf("London", "New York", "Tokyo", "Paris", "Berlin")
        }
    }

    fun startUpdates() {
        val intervalMinutes = settingsManager.getUpdateInterval().toLong()
        scheduleWork(intervalMinutes)
        fetchLatest(intervalMinutes)
    }

    private fun scheduleWork(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WeatherWorker>(
            intervalMinutes, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        workManager.enqueue(workRequest)
    }

    private fun fetchLatest(intervalMinutes: Long) {
        viewModelScope.launch {
            while (true) {
                _latestWeather.value = Resource.Loading()
                val results = mutableListOf<CurrentWeatherResponse>()
                var hasError = false
                
                val cities = getLocalizedCities()
                for (city in cities) {
                    val result = repository.fetchWeatherByCity(city, Constants.API_KEY)
                    if (result is Resource.Success && result.data != null) {
                        results.add(result.data)
                    } else {
                        hasError = true
                    }
                }

                if (hasError && results.isEmpty()) {
                    _latestWeather.value = Resource.Error("Failed to fetch updates")
                } else {
                    _latestWeather.value = Resource.Success(results)
                }
                
                delay(intervalMinutes * 60 * 1000) // Convert minutes to milliseconds
            }
        }
    }
}