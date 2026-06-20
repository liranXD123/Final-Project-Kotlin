package com.example.finalprojectweatherapp.utils

import android.util.TypedValue
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.finalprojectweatherapp.R

/**
 * Glide helper for weather icons in the Favorites RecyclerView.
 * Caches images and handles icon codes stored in Room.
 */
object WeatherIconLoader {

    private const val ICON_BASE_URL = "https://openweathermap.org/img/wn/"
    private const val ICON_SUFFIX = "@4x.png"

    fun iconUrl(iconCode: String?): String {
        val code = iconCode?.trim()?.takeIf { it.isNotEmpty() } ?: "01d"
        return "$ICON_BASE_URL$code$ICON_SUFFIX"
    }

    /**
     * Room stores short codes ("01d") or legacy full URLs — normalizes before loading.
     */
    fun iconUrlFromStored(stored: String?): String {
        if (stored.isNullOrBlank()) return iconUrl(null)
        if (stored.startsWith("http", ignoreCase = true)) {
            return when {
                stored.contains("@4x.png") -> stored
                stored.contains("@2x.png") -> stored.replace("@2x.png", "@4x.png")
                else -> stored
            }
        }
        return iconUrl(stored)
    }

    fun load(imageView: ImageView, iconCode: String?) {
        loadUrl(imageView, iconUrl(iconCode))
    }

    /** Called from FavoritesAdapter — loads icon from WeatherEntity.iconUrl field. */
    fun loadFromStored(imageView: ImageView, iconUrlOrCode: String?) {
        loadUrl(imageView, iconUrlFromStored(iconUrlOrCode))
    }

    /** Glide: disk cache + placeholder while loading + error fallback image. */
    private fun loadUrl(imageView: ImageView, url: String) {
        val targetPx = resolveTargetSizePx(imageView)
        Glide.with(imageView)
            .load(url)
            .override(targetPx, targetPx)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_weather_placeholder)
            .error(R.drawable.ic_weather_placeholder)
            .into(imageView)
    }

    private fun resolveTargetSizePx(imageView: ImageView): Int {
        val dm = imageView.resources.displayMetrics
        val layoutPx = when {
            imageView.width > 0 -> imageView.width
            imageView.layoutParams.width > 0 -> imageView.layoutParams.width
            else -> TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                96f,
                dm
            ).toInt()
        }
        return layoutPx.coerceAtLeast((96 * dm.density).toInt())
    }
}
