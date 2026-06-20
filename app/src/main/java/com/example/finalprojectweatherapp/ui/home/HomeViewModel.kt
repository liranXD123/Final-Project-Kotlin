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

// viewmodel for managing the home screen data and weather state
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    // private mutable live data for weather state
    private val _weatherState = MutableLiveData<Resource<CurrentWeatherResponse>>()
    // public live data for observing weather state changes
    val weatherState: LiveData<Resource<CurrentWeatherResponse>> = _weatherState

    // live data indicating if temperature unit is celsius
    val isCelsius = settingsManager.isCelsius.asLiveData()

    // holds the latitude of the last successful location
    var lastLatitude: Double? = null
        private set
    // holds the longitude of the last successful location
    var lastLongitude: Double? = null
        private set

    // reference to the current weather fetching coroutine job
    private var fetchJob: Job? = null

    /**
     * stores the provided location coordinates
     * @param lat the latitude coordinate
     * @param lon the longitude coordinate
     * @return nothing
     */
    fun setLastLocation(lat: Double, lon: Double) {
        // updates the last latitude property
        lastLatitude = lat
        // updates the last longitude property
        lastLongitude = lon
    }

    /**
     * checks if location coordinates are already cached
     * @param none
     * @return true if both coordinates are present
     */
    fun hasCachedLocation(): Boolean = lastLatitude != null && lastLongitude != null

    /**
     * loads weather data for the specified location
     * @param lat the latitude to fetch for
     * @param lon the longitude to fetch for
     * @return nothing
     */
    fun loadWeatherForLocation(lat: Double, lon: Double) {
        // saves the location coordinates
        setLastLocation(lat, lon)
        // cancels any existing fetch job
        fetchJob?.cancel()
        // launches a coroutine to fetch weather data
        fetchJob = viewModelScope.launch {
            // posts loading state to the live data
            _weatherState.value = Resource.Loading()
            // performs api request and updates state with result
            _weatherState.value = repository.fetchCurrentWeather(lat, lon, Constants.API_KEY)
        }
    }

    /**
     * refreshes weather data using stored coordinates
     * @param none
     * @return true if refresh was initiated, false if no coordinates found
     */
    fun refreshWithCachedLocation(): Boolean {
        // retrieves latitude or returns false if missing
        val lat = lastLatitude ?: return false
        // retrieves longitude or returns false if missing
        val lon = lastLongitude ?: return false
        // triggers a weather load with retrieved coordinates
        loadWeatherForLocation(lat, lon)
        // returns true to indicate success
        return true
    }
}
