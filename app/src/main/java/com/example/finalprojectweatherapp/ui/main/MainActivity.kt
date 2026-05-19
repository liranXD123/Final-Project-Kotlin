package com.example.finalprojectweatherapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.finalprojectweatherapp.R
import com.example.finalprojectweatherapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.latestWeatherFragment,
        R.id.forecastFragment,
        R.id.favoritesFragment,
        R.id.settingsFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)

        setupActionBarWithNavController(navController, appBarConfiguration)
        setupDestinationListener()

        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun setupDestinationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in topLevelDestinations
            val isPollution = destination.id == R.id.pollutionFragment

            setBottomNavigationVisible(isTopLevel)

            when {
                isPollution -> {
                    supportActionBar?.apply {
                        setDisplayShowTitleEnabled(true)
                        title = getString(R.string.air_pollution_data)
                    }
                    binding.toolbar.navigationContentDescription =
                        getString(R.string.navigate_up_desc)
                }
                isTopLevel -> {
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                }
                else -> {
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
            }
        }
    }

    private fun setBottomNavigationVisible(visible: Boolean) {
        val bottomNav = binding.bottomNavigationView
        if (visible) {
            bottomNav.isVisible = true
            bottomNav.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(BOTTOM_NAV_ANIM_MS)
                .start()
        } else if (bottomNav.isVisible) {
            bottomNav.animate()
                .translationY(bottomNav.height.toFloat())
                .alpha(0f)
                .setDuration(BOTTOM_NAV_ANIM_MS)
                .withEndAction {
                    bottomNav.isVisible = false
                    bottomNav.translationY = 0f
                    bottomNav.alpha = 1f
                }
                .start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        private const val BOTTOM_NAV_ANIM_MS = 220L
    }
}
