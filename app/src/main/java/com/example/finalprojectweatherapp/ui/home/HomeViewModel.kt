package com.example.finalprojectweatherapp.ui.home

import androidx.lifecycle.asLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import com.example.finalprojectweatherapp.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _weatherState = MutableLiveData<Resource<CurrentWeatherResponse>>()
    val weatherState: LiveData<Resource<CurrentWeatherResponse>> = _weatherState
    val isCelsius = settingsManager.isCelsius.asLiveData()
    var lastLatitude: Double? = null
        private set // ro outside, rw inside
    var lastLongitude: Double? = null
        private set

    // reference to the current weather fetching job
    private var fetchJob: Job? = null

    // stores the last location coordinates
    fun setLastLocation(lat: Double, lon: Double) {
        lastLatitude = lat
        lastLongitude = lon
    }

    // boolean to check if cached loc is present
    fun hasCachedLocation(): Boolean = lastLatitude != null && lastLongitude != null

    // load weather data for specified location
    fun loadWeatherForLocation(lat: Double, lon: Double) {
        // cache this location
        setLastLocation(lat, lon)
        // cancels any existing job
        fetchJob?.cancel()
        // fetches weather data
        fetchJob = viewModelScope.launch {
            _weatherState.value = Resource.Loading()
            // performs api request and updates state with result
            _weatherState.value = repository.fetchCurrentWeather(lat, lon, Constants.API_KEY)
        }
    }

    // refresh data for the cached location (quick update)
    // returns false if no cached data location is present
    fun refreshWithCachedLocation(): Boolean {
        val lat = lastLatitude ?: return false
        val lon = lastLongitude ?: return false
        loadWeatherForLocation(lat, lon)
        return true
    }
}
