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

/**
 * Single-activity app shell.
 *
 * - NavHostFragment loads nav_graph.xml and swaps fragments.
 * - Bottom nav IDs match fragment IDs — setupWithNavController handles tab switching.
 * - Secondary screens (Add Favorite, Pollution) hide the bottom bar.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Screens that show the bottom navigation bar (main tabs).
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

        // NavController drives all fragment navigation from nav_graph.xml.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations show the Up button only when needed (not on main tabs).
        appBarConfiguration = AppBarConfiguration(topLevelDestinations)

        setupActionBarWithNavController(navController, appBarConfiguration)
        setupDestinationListener()

        // Links bottom nav menu item IDs to matching fragment IDs in nav_graph — no manual navigate() needed.
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    /**
     * Runs on every screen change — adjusts toolbar title and bottom nav visibility.
     * Add Favorite is NOT top-level → bottom bar hides so user focuses on the form.
     */
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
                    // Add Favorite and other stacked screens — show title, hide bottom nav.
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
            }
        }
    }

    /** Animates bottom nav in/out when entering or leaving secondary destinations. */
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

    /** Toolbar back arrow — pops back stack (e.g. Add Favorite → Favorites). */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        private const val BOTTOM_NAV_ANIM_MS = 220L
    }
}
