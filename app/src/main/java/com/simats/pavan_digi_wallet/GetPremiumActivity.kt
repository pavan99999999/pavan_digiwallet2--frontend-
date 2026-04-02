package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.addCallback

class GetPremiumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_premium)

        // Dark status bar
        window.statusBarColor = Color.parseColor("#1E1E1E")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // Top toolbar actions
        findViewById<View>(R.id.btn_back)?.setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.slide_out_right)
        }

        findViewById<View>(R.id.btn_more)?.setOnClickListener {
            Toast.makeText(this, "More options coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Pricing Card logic
        findViewById<View>(R.id.card_lifetime)?.setOnClickListener {
            startActivity(Intent(this, GooglePlayPaymentActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, 0)
        }

        findViewById<View>(R.id.card_yearly)?.setOnClickListener {
            startActivity(Intent(this, GooglePlayPaymentActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, 0)
        }

        findViewById<View>(R.id.card_monthly)?.setOnClickListener {
            startActivity(Intent(this, GooglePlayPaymentActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, 0)
        }

        // Underline links
        val tvTerms = findViewById<TextView>(R.id.tv_terms)
        tvTerms?.paintFlags = tvTerms?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG

        val tvPrivacy = findViewById<TextView>(R.id.tv_privacy)
        tvPrivacy?.paintFlags = tvPrivacy?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(0, android.R.anim.slide_out_right)
        }
    }
}
