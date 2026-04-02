package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.addCallback

class ChooseAccountTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_account_type)

        // Dark status bar for dark background
        window.statusBarColor = Color.parseColor("#111318")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // Close / back button
        findViewById<View>(R.id.btn_close)?.setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }

        // Bank Sync
        findViewById<View>(R.id.card_bank_sync)?.setOnClickListener {
            Toast.makeText(this, "Bank Sync coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Investments
        findViewById<View>(R.id.card_investments)?.setOnClickListener {
            startActivity(Intent(this, InvestmentPremiumActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, 0)
        }

        // File Import
        findViewById<View>(R.id.card_file_import)?.setOnClickListener {
            Toast.makeText(this, "File Import coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Manual Input
        findViewById<View>(R.id.card_manual_input)?.setOnClickListener {
            Toast.makeText(this, "Manual Input coming soon!", Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }
}
