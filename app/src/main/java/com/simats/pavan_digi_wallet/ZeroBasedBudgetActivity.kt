package com.simats.pavan_digi_wallet

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class ZeroBasedBudgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_zero_based_budget)

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val etMonthlyIncome = findViewById<EditText>(R.id.et_monthly_income)

        findViewById<View>(R.id.btn_continue_planning).setOnClickListener {
            val incomeStr = etMonthlyIncome.text.toString().trim()
            val income = incomeStr.toDoubleOrNull()
            if (income == null || income <= 0) {
                android.widget.Toast.makeText(this, "Please enter your monthly income", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = android.content.Intent(this, BudgetPlanningActivity::class.java)
            intent.putExtra("monthly_income", income)
            startActivity(intent)
        }
    }
}
