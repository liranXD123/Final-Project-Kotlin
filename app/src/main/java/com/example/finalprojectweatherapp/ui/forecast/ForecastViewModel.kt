package com.example.finalprojectweatherapp.ui.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.remote.models.ForecastResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _forecastState = MutableLiveData<Resource<ForecastResponse>>()
    val forecastState: LiveData<Resource<ForecastResponse>> = _forecastState

    fun getForecast(lat: Double, lon: Double) {
        _forecastState.value = Resource.Loading()
        viewModelScope.launch {
            _forecastState.value = repository.fetchForecast(lat, lon, Constants.API_KEY)
        }
    }
}