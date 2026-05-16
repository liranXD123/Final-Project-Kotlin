package com.example.finalprojectweatherapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("list") val forecastList: List<ForecastItem>
)

data class ForecastItem(
    @SerializedName("dt_txt") val dateTime: String,
    @SerializedName("main") val main: MainStats,
    @SerializedName("weather") val weatherConditions: List<WeatherCondition>
)