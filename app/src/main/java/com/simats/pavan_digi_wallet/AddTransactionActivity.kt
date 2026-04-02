package com.simats.pavan_digi_wallet

import android.os.Bundle
import android.widget.TextView
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var tabExpense: TextView
    private lateinit var tabIncome: TextView
    private lateinit var tabTransfer: TextView
    private lateinit var rvCategories: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        // Dark Theme Status Bar
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        tabExpense = findViewById(R.id.tab_expense)
        tabIncome = findViewById(R.id.tab_income)
        tabTransfer = findViewById(R.id.tab_transfer)

        rvCategories = findViewById(R.id.rv_categories)
        rvCategories.layoutManager = GridLayoutManager(this, 4)

        // Using specific icons for categories
        val expenseCategories = listOf(
            Category("Transport", R.drawable.ic_transport),
            Category("Shopping", R.drawable.ic_cart),
            Category("Car", R.drawable.ic_car_alt),
            Category("Social", R.drawable.ic_social),
            Category("Electronics", R.drawable.ic_electronics),
            Category("Travel", R.drawable.ic_travel),
            Category("Health", R.drawable.ic_health),
            Category("Pets", R.drawable.ic_heart),
            Category("Repairs", R.drawable.ic_repairs),
            Category("Housing", R.drawable.ic_home),
            Category("Gifts", R.drawable.ic_donations),
            Category("Education", R.drawable.ic_education),
            Category("Lottery", R.drawable.ic_lottery),
            Category("Snacks", R.drawable.ic_food_fork),
            Category("Kids", R.drawable.ic_kids),
            Category("Groceries", R.drawable.ic_cart)
        )

        val incomeCategories = listOf(
            Category("Salary", R.drawable.ic_bank),
            Category("Investments", R.drawable.ic_investments),
            Category("Part-Time", R.drawable.ic_briefcase),
            Category("Bonus", R.drawable.ic_money_bag),
            Category("Others", R.drawable.ic_more_horizontal)
        )

        fun updateTabs(activeTab: TextView) {
            val tabs = listOf(tabExpense, tabIncome, tabTransfer)
            tabs.forEach { tab ->
                if (tab == activeTab) {
                    tab.setBackgroundResource(R.drawable.bg_tab_pill)
                    tab.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_blue)
                    tab.setTextColor(android.graphics.Color.WHITE)
                } else {
                    tab.backgroundTintList = null
                    tab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    tab.setTextColor(android.graphics.Color.parseColor("#6F767E"))
                }
            }
        }

        tabExpense.setOnClickListener {
            updateTabs(tabExpense)
            rvCategories.adapter = CategoryAdapter(expenseCategories, "expense")
        }

        tabIncome.setOnClickListener {
            updateTabs(tabIncome)
            rvCategories.adapter = CategoryAdapter(incomeCategories, "income")
        }

        tabTransfer.setOnClickListener {
            updateTabs(tabTransfer)
            rvCategories.adapter = CategoryAdapter(emptyList(), "transfer") // Placeholder
        }

        // Default tab selection based on Intent
        val typeIntent = intent.getStringExtra("type") ?: "expense"
        if (typeIntent == "income") {
            updateTabs(tabIncome)
            rvCategories.adapter = CategoryAdapter(incomeCategories, "income")
        } else {
            updateTabs(tabExpense)
            rvCategories.adapter = CategoryAdapter(expenseCategories, "expense")
        }
    }
}
