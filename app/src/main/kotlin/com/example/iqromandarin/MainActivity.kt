package com.example.iqromandarin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.iqromandarin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val REQUEST_MICROPHONE = 100
        const val PREF_DARK_MODE = "dark_mode"
        const val PREF_NAME = "iqro_mandarin_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark mode before setContentView
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val darkMode = prefs.getBoolean(PREF_DARK_MODE, false)
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Load Dashboard on start
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment(), "Dashboard")
        }

        // Request microphone permission
        requestMicPermission()

        // Schedule daily review notification
        ReviewNotificationWorker.scheduleDaily(this)
    }

    fun loadFragment(fragment: Fragment, title: String = "") {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(if (title == "Dashboard") null else title)
            .commit()
        if (title.isNotEmpty()) {
            supportActionBar?.title = title
        }
    }

    fun toggleDarkMode() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val current = prefs.getBoolean(PREF_DARK_MODE, false)
        prefs.edit().putBoolean(PREF_DARK_MODE, !current).apply()
        if (!current) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_MICROPHONE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Permission handled, mic will be available
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
