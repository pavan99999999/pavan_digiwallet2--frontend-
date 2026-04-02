package com.simats.pavan_digi_wallet

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<View>(R.id.btn_close).setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }

        findViewById<View>(R.id.btn_planned_payments).setOnClickListener {
            val intent = android.content.Intent(this, PlannedBudgetActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_smart_purchase_planner).setOnClickListener {
            val intent = android.content.Intent(this, SmartPurchasePlannerActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_net_worth).setOnClickListener {
            val intent = android.content.Intent(this, NetWorthActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_get_premium).setOnClickListener {
            val intent = android.content.Intent(this, GetPremiumActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_subscriptions).setOnClickListener {
            val intent = android.content.Intent(this, AddSubscriptionActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_logout).setOnClickListener {
            // Clear shared preferences
            val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
