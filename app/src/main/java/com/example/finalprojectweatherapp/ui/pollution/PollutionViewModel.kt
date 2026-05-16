package com.example.finalprojectweatherapp.ui.pollution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.remote.models.PollutionResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PollutionViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _pollutionState = MutableLiveData<Resource<PollutionResponse>>()
    val pollutionState: LiveData<Resource<PollutionResponse>> = _pollutionState

    fun getPollutionData(lat: Double, lon: Double) {
        _pollutionState.value = Resource.Loading()
        viewModelScope.launch {
            _pollutionState.value = repository.fetchAirPollution(lat, lon, Constants.API_KEY)
        }
    }
}