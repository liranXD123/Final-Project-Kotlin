package com.example.finalprojectweatherapp.utils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.finalprojectweatherapp.R

/**
 * OpenWeatherMap air pollution index: 1 = Good … 5 = Very Poor.
 */
enum class AqiLevel(
    val index: Int,
    @ColorRes val colorRes: Int,
    @ColorRes val backgroundRes: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val adviceRes: Int
) {
    GOOD(
        index = 1,
        colorRes = R.color.aqi_good,
        backgroundRes = R.color.aqi_good_bg,
        iconRes = R.drawable.ic_aqi_good,
        labelRes = R.string.aqi_level_good,
        descriptionRes = R.string.aqi_desc_good,
        adviceRes = R.string.aqi_advice_good
    ),
    FAIR(
        index = 2,
        colorRes = R.color.aqi_fair,
        backgroundRes = R.color.aqi_fair_bg,
        iconRes = R.drawable.ic_aqi_fair,
        labelRes = R.string.aqi_level_fair,
        descriptionRes = R.string.aqi_desc_fair,
        adviceRes = R.string.aqi_advice_fair
    ),
    MODERATE(
        index = 3,
        colorRes = R.color.aqi_moderate,
        backgroundRes = R.color.aqi_moderate_bg,
        iconRes = R.drawable.ic_aqi_moderate,
        labelRes = R.string.aqi_level_moderate,
        descriptionRes = R.string.aqi_desc_moderate,
        adviceRes = R.string.aqi_advice_moderate
    ),
    POOR(
        index = 4,
        colorRes = R.color.aqi_poor,
        backgroundRes = R.color.aqi_poor_bg,
        iconRes = R.drawable.ic_aqi_poor,
        labelRes = R.string.aqi_level_poor,
        descriptionRes = R.string.aqi_desc_poor,
        adviceRes = R.string.aqi_advice_poor
    ),
    VERY_POOR(
        index = 5,
        colorRes = R.color.aqi_very_poor,
        backgroundRes = R.color.aqi_very_poor_bg,
        iconRes = R.drawable.ic_aqi_very_poor,
        labelRes = R.string.aqi_level_very_poor,
        descriptionRes = R.string.aqi_desc_very_poor,
        adviceRes = R.string.aqi_advice_very_poor
    );

    companion object {
        fun fromIndex(aqi: Int): AqiLevel = entries.find { it.index == aqi } ?: GOOD
    }
}
