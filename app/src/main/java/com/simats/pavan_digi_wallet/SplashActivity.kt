package com.simats.pavan_digi_wallet

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Dark Mode for a consistent premium look, regardless of system settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val logoImage = findViewById<ImageView>(R.id.logoImage)
        val titleText = findViewById<TextView>(R.id.titleText)
        val subtitleText = findViewById<TextView>(R.id.subtitleText)
        val progressBar = findViewById<LinearProgressIndicator>(R.id.progressBar)

        // Set initial state for animation
        logoImage.alpha = 0f
        logoImage.translationY = 50f
        titleText.alpha = 0f
        titleText.translationY = 50f
        subtitleText.alpha = 0f
        subtitleText.translationY = 50f
        progressBar.alpha = 0f

        // Animate elements to fade in and slide up
        logoImage.animate().alpha(1f).translationY(0f).setDuration(800).start()
        titleText.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(300).start()
        subtitleText.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(500).start()
        progressBar.animate().alpha(1f).setDuration(800).setStartDelay(700).start()

        // Wait a few seconds then check login status
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
            val accessToken = sharedPref.getString("access_token", null)

            if (accessToken != null) {
                // User has a session
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User needs to log in
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish()
            
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }, 3000)
    }
}
