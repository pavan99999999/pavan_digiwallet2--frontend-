package com.simats.pavan_digi_wallet

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.addCallback

class GooglePlayPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_play_payment)

        // Light status bar with dark icons
        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Top toolbar actions
        findViewById<View>(R.id.btn_close)?.setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.slide_out_right)
        }

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(0, android.R.anim.slide_out_right)
        }
    }
}
