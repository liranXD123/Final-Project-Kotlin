package com.example.finalprojectweatherapp.ui.settings

import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectweatherapp.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val isCelsius = settingsManager.isCelsius.asLiveData()
    val updateInterval = settingsManager.updateInterval.asLiveData()

    fun setUnit(celsius: Boolean) {
        settingsManager.setCelsius(celsius)
    }

    fun setUpdateInterval(minutes: Int) {
        settingsManager.setUpdateInterval(minutes)
    }
}
