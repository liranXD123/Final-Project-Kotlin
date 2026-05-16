package com.example.finalprojectweatherapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class PollutionResponse(
    @SerializedName("list") val list: List<PollutionItem>
)

data class PollutionItem(
    @SerializedName("main") val main: PollutionMain,
    @SerializedName("components") val components: Map<String, Double>
)

data class PollutionMain(
    @SerializedName("aqi") val aqi: Int // Air Quality Index: 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
)