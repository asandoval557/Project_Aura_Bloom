package com.example.project_aura_bloom

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import com.example.project_aura_bloom.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.activity.OnBackPressedCallback


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)  // Firebase initialization

        //Initialize Firebase app check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Setup for drawer and nav view for the side menu
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Setting up nav for switch management between screens
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Toggle button for closing and opening menu drawer
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        // Syncing for the toggle state and menu drawer state
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Forming the app bar to work with the drawer
        appBarConfiguration = AppBarConfiguration(setOf(R.id.HomeScreenFragment,
            R.id.CalmZoneFragment,
            R.id.MoodProgressFragment), drawerLayout)


        setupActionBarWithNavController(navController, appBarConfiguration)

        // Display hot-dog menu as default icon for side drawer and syncing when returning to home screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.HomeScreenFragment) {
                // Only show hot-dog menu icon on home screen
                toggle.isDrawerIndicatorEnabled = true
                binding.toolbar.setNavigationIcon(R.drawable.ic_hotdog_menu)
                binding.toolbar.setNavigationOnClickListener {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                }
            } else {
                // Show back arrow on fragment screens
                toggle.isDrawerIndicatorEnabled = false
                supportActionBar?.setDisplayShowTitleEnabled(true)
                binding.toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
                binding.toolbar.setNavigationOnClickListener {
                    navController.navigateUp()
                }
            }
        }

        // Setting up the click event for the hot dog menu
        binding.toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Syncing for the toggle state and menu drawer state
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle nav drawer item selection
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navController.navigate(R.id.HomeScreenFragment)
                R.id.nav_profile -> navController.navigate(R.id.ProfileFragment)
                R.id.nav_calm_zone -> navController.navigate(R.id.CalmZoneFragment)
                R.id.nav_mood_progress -> navController.navigate(R.id.MoodProgressFragment)
                R.id.nav_exercise -> {
                    // Handle exercises navigation
                }
                R.id.nav_settings -> {
                    // Handle settings navigation
                }
                R.id.nav_help -> {
                    // Handle help center navigation
                }
                R.id.nav_sign_out -> signOutUser()

            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close drawer after selection
            true
        }

        // Handle back press event
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if the drawer is open; if so, close it. Otherwise, do default back.
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun signOutUser() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        // Navigate back to the login screen
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.LoginFragment)

        // Clear the back stack to prevent users going back after signing off
        navController.popBackStack(R.id.HomeScreenFragment, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}