package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.addCallback

class InvestmentPremiumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investment_premium)

        // Dark status bar for dark background
        window.statusBarColor = Color.parseColor("#3D3D3D")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // Close button
        findViewById<View>(R.id.btn_close)?.setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }

        // Show Premium Plans button
        findViewById<View>(R.id.btn_show_plans)?.setOnClickListener {
            startActivity(Intent(this, GetPremiumActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, 0)
        }

        // Try for Free button
        findViewById<View>(R.id.btn_try_free)?.setOnClickListener {
            Toast.makeText(this, "Free trial coming soon!", Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }
}
