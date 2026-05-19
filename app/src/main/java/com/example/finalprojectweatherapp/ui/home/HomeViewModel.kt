package com.example.finalprojectweatherapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    // _weatherState is private so the Fragment can't accidentally edit the data
    private val _weatherState = MutableLiveData<Resource<CurrentWeatherResponse>>()

    // weatherState is public, but read-only for the Fragment to observe
    val weatherState: LiveData<Resource<CurrentWeatherResponse>> = _weatherState

    var lastLatitude: Double? = null
        private set
    var lastLongitude: Double? = null
        private set

    fun setLastLocation(lat: Double, lon: Double) {
        lastLatitude = lat
        lastLongitude = lon
    }

    fun loadWeatherForLocation(lat: Double, lon: Double) {
        setLastLocation(lat, lon)
        // 1. Tell the UI we are loading (so it can show a progress bar)
        _weatherState.value = Resource.Loading()

        // 2. Launch a Coroutine to fetch data off the main thread
        viewModelScope.launch {
            val result = repository.fetchCurrentWeather(lat, lon, com.example.finalprojectweatherapp.utils.Constants.API_KEY)

            // 3. Post the success or error result back to the UI
            _weatherState.value = result
        }
    }
}