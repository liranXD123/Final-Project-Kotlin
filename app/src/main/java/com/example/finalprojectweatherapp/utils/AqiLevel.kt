// package declaration for the utility classes
package com.example.finalprojectweatherapp.utils

// import the color resource annotation from androidx
import androidx.annotation.ColorRes
// import the drawable resource annotation from androidx
import androidx.annotation.DrawableRes
// import the string resource annotation from androidx
import androidx.annotation.StringRes
// import the local project resource class
import com.example.finalprojectweatherapp.R

// openweathermap air pollution index mapping where 1 is good and 5 is very poor
// define an enum class to bundle air quality levels with their related ui resources
enum class AqiLevel(
    // the numeric integer index representing this air quality level
    val index: Int,
    // the resource id for the text color associated with this level
    @ColorRes val colorRes: Int,
    // the resource id for the background color associated with this level
    @ColorRes val backgroundRes: Int,
    // the resource id for the icon graphic representing this level
    @DrawableRes val iconRes: Int,
    // the resource id for the short display label of this level
    @StringRes val labelRes: Int,
    // the resource id for the detailed descriptive text of this level
    @StringRes val descriptionRes: Int,
    // the resource id providing health recommendations for this level
    @StringRes val adviceRes: Int
) {
    // define the enum entry for a good air quality level
    GOOD(
        // set the index value to 1
        index = 1,
        // set the color resource to the good level text color
        colorRes = R.color.aqi_good,
        // set the background resource to the good level background color
        backgroundRes = R.color.aqi_good_bg,
        // set the icon resource to the good level drawable
        iconRes = R.drawable.ic_aqi_good,
        // set the label resource to the good level string
        labelRes = R.string.aqi_level_good,
        // set the description resource to the good level string
        descriptionRes = R.string.aqi_desc_good,
        // set the advice resource to the good level health string
        adviceRes = R.string.aqi_advice_good
    ),
    // define the enum entry for a fair air quality level
    FAIR(
        // set the index value to 2
        index = 2,
        // set the color resource to the fair level text color
        colorRes = R.color.aqi_fair,
        // set the background resource to the fair level background color
        backgroundRes = R.color.aqi_fair_bg,
        // set the icon resource to the fair level drawable
        iconRes = R.drawable.ic_aqi_fair,
        // set the label resource to the fair level string
        labelRes = R.string.aqi_level_fair,
        // set the description resource to the fair level string
        descriptionRes = R.string.aqi_desc_fair,
        // set the advice resource to the fair level health string
        adviceRes = R.string.aqi_advice_fair
    ),
    // define the enum entry for a moderate air quality level
    MODERATE(
        // set the index value to 3
        index = 3,
        // set the color resource to the moderate level text color
        colorRes = R.color.aqi_moderate,
        // set the background resource to the moderate level background color
        backgroundRes = R.color.aqi_moderate_bg,
        // set the icon resource to the moderate level drawable
        iconRes = R.drawable.ic_aqi_moderate,
        // set the label resource to the moderate level string
        labelRes = R.string.aqi_level_moderate,
        // set the description resource to the moderate level string
        descriptionRes = R.string.aqi_desc_moderate,
        // set the advice resource to the moderate level health string
        adviceRes = R.string.aqi_advice_moderate
    ),
    // define the enum entry for a poor air quality level
    POOR(
        // set the index value to 4
        index = 4,
        // set the color resource to the poor level text color
        colorRes = R.color.aqi_poor,
        // set the background resource to the poor level background color
        backgroundRes = R.color.aqi_poor_bg,
        // set the icon resource to the poor level drawable
        iconRes = R.drawable.ic_aqi_poor,
        // set the label resource to the poor level string
        labelRes = R.string.aqi_level_poor,
        // set the description resource to the poor level string
        descriptionRes = R.string.aqi_desc_poor,
        // set the advice resource to the poor level health string
        adviceRes = R.string.aqi_advice_poor
    ),
    // define the enum entry for a very poor air quality level
    VERY_POOR(
        // set the index value to 5
        index = 5,
        // set the color resource to the very poor level text color
        colorRes = R.color.aqi_very_poor,
        // set the background resource to the very poor level background color
        backgroundRes = R.color.aqi_very_poor_bg,
        // set the icon resource to the very poor level drawable
        iconRes = R.drawable.ic_aqi_very_poor,
        // set the label resource to the very poor level string
        labelRes = R.string.aqi_level_very_poor,
        // set the description resource to the very poor level string
        descriptionRes = R.string.aqi_desc_very_poor,
        // set the advice resource to the very poor level health string
        adviceRes = R.string.aqi_advice_very_poor
    );

    // declare a companion object to hold static utility methods
    companion object {
        /*
         * retrieves the correct aqilevel enum based on the provided numeric index.
         * * parameters:
         * aqi - the integer index returned from the weather api mapping to a level
         * * returns: the matching aqilevel, defaulting to good if the index is out of bounds
         */
        // search through all enum entries to find a matching index, returning good as a fallback
        fun fromIndex(aqi: Int): AqiLevel = entries.find { it.index == aqi } ?: GOOD
    }
}