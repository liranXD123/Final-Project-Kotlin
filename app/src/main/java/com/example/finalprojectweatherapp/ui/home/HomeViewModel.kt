package com.example.finalprojectweatherapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableLiveData<Resource<CurrentWeatherResponse>>()
    val weatherState: LiveData<Resource<CurrentWeatherResponse>> = _weatherState

    var lastLatitude: Double? = null
        private set
    var lastLongitude: Double? = null
        private set

    private var fetchJob: Job? = null

    fun setLastLocation(lat: Double, lon: Double) {
        lastLatitude = lat
        lastLongitude = lon
    }

    fun hasCachedLocation(): Boolean = lastLatitude != null && lastLongitude != null

    fun loadWeatherForLocation(lat: Double, lon: Double) {
        setLastLocation(lat, lon)
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _weatherState.value = Resource.Loading()
            _weatherState.value = repository.fetchCurrentWeather(lat, lon, Constants.API_KEY)
        }
    }

    /** Re-fetches weather for the last successful coordinates (fast refresh). */
    fun refreshWithCachedLocation(): Boolean {
        val lat = lastLatitude ?: return false
        val lon = lastLongitude ?: return false
        loadWeatherForLocation(lat, lon)
        return true
    }
}
