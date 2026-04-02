package com.simats.pavan_digi_wallet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class BudgetPlanningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget_planning)

        val incomeFromIntent = intent.getDoubleExtra("monthly_income", 0.0)
        totalIncome = incomeFromIntent
        remainingBudget = incomeFromIntent

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_get_ai_recommendation).setOnClickListener {
            fetchAiRecommendations()
        }

        setupCategories()
    }

    private fun fetchAiRecommendations() {
        val tvPotentialSavings = findViewById<TextView>(R.id.tv_ai_potential_savings)
        val layoutTips = findViewById<LinearLayout>(R.id.layout_ai_tips)
        
        tvPotentialSavings.text = "AI is analyzing..."
        layoutTips.removeAllViews()
        layoutTips.visibility = View.GONE

        // Preparing a dummy/simulated request for demonstration
        // In a real app, these values would come from the database/summaries
        val request = BudgetRequest(
            monthlyIncome = totalIncome,
            diningSpend = 5000.0,
            shoppingSpend = 4000.0,
            transportSpend = 3000.0,
            groceriesSpend = 6000.0,
            entertainmentSpend = 2000.0,
            lastMonthDining = 6500.0,
            lastMonthShopping = 3500.0,
            lastMonthTransport = 2800.0,
            lastMonthGroceries = 5800.0,
            lastMonthEntertainment = 2500.0,
            diningChangePercent = -23.0,
            shoppingChangePercent = 14.0,
            transportChangePercent = 7.0,
            groceriesChangePercent = 3.0,
            entertainmentChangePercent = -20.0,
            totalSpending = 20000.0,
            savingsChange = 2500.0
        )

        RetrofitClient.apiService.getBudgetRecommendations(request).enqueue(object : retrofit2.Callback<BudgetRecommendationResponse> {
            override fun onResponse(call: retrofit2.Call<BudgetRecommendationResponse>, response: retrofit2.Response<BudgetRecommendationResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    tvPotentialSavings.text = "Potential Monthly Savings: ${body?.potentialSavings}"
                    
                    layoutTips.visibility = View.VISIBLE
                    body?.tips?.forEach { tip ->
                        val tvTip = TextView(this@BudgetPlanningActivity)
                        tvTip.text = tip
                        tvTip.setTextColor(android.graphics.Color.WHITE)
                        tvTip.textSize = 14f
                        tvTip.setPadding(0, 8, 0, 8)
                        layoutTips.addView(tvTip)
                    }
                } else {
                    tvPotentialSavings.text = "Recommendation Unavailable"
                    android.widget.Toast.makeText(this@BudgetPlanningActivity, "AI model not responding", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<BudgetRecommendationResponse>, t: Throwable) {
                tvPotentialSavings.text = "Check Connection"
                android.widget.Toast.makeText(this@BudgetPlanningActivity, "Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var totalIncome: Double = 0.0
    private var remainingBudget: Double = 0.0
    private val categoryAmounts = mutableMapOf<Int, Double>()

    private fun setupCategories() {
        val tvTotalIncome = findViewById<TextView>(R.id.tv_total_income)
        // Set initial total income to UI
        tvTotalIncome.text = "₹${totalIncome.toInt()}"

        // Rent
        val rent = findViewById<View>(R.id.item_rent)
        rent.findViewById<TextView>(R.id.tv_category_name).text = "Rent/EMI"
        rent.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_home)
        rent.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#E67E22"))
        setupCategoryClickListeners(rent, R.id.item_rent)

        // Groceries
        val groceries = findViewById<View>(R.id.item_groceries)
        groceries.findViewById<TextView>(R.id.tv_category_name).text = "Groceries"
        groceries.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_cart)
        groceries.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#2ECC71"))
        setupCategoryClickListeners(groceries, R.id.item_groceries)

        // Utilities
        val utilities = findViewById<View>(R.id.item_utilities)
        utilities.findViewById<TextView>(R.id.tv_category_name).text = "Utilities"
        utilities.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_electricity)
        utilities.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#3498DB"))
        setupCategoryClickListeners(utilities, R.id.item_utilities)

        // Transport
        val transport = findViewById<View>(R.id.item_transport)
        transport.findViewById<TextView>(R.id.tv_category_name).text = "Transport"
        transport.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_transport)
        transport.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#E74C3C"))
        setupCategoryClickListeners(transport, R.id.item_transport)

        // Emergency Fund
        val emergency = findViewById<View>(R.id.item_emergency)
        emergency.findViewById<TextView>(R.id.tv_category_name).text = "Emergency Fund"
        emergency.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_shield)
        emergency.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#95A5A6"))
        setupCategoryClickListeners(emergency, R.id.item_emergency)

        // Savings
        val savings = findViewById<View>(R.id.item_savings)
        savings.findViewById<TextView>(R.id.tv_category_name).text = "Savings"
        savings.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_money_bag)
        savings.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#F1C40F"))
        setupCategoryClickListeners(savings, R.id.item_savings)

        // Healthcare
        val healthcare = findViewById<View>(R.id.item_healthcare)
        healthcare.findViewById<TextView>(R.id.tv_category_name).text = "Healthcare"
        healthcare.findViewById<ImageView>(R.id.img_category).setImageResource(R.drawable.ic_heart)
        healthcare.findViewById<ImageView>(R.id.img_category).setColorFilter(android.graphics.Color.parseColor("#FF4D4D"))
        setupCategoryClickListeners(healthcare, R.id.item_healthcare)

        updateHeaderDisplays()
    }

    private fun setupCategoryClickListeners(view: View, categoryId: Int) {
        categoryAmounts[categoryId] = 0.0

        view.findViewById<View>(R.id.btn_minus_500).setOnClickListener {
            updateCategoryAmount(categoryId, view, -500.0)
        }
        view.findViewById<View>(R.id.btn_plus_500).setOnClickListener {
            updateCategoryAmount(categoryId, view, 500.0)
        }
        view.findViewById<View>(R.id.btn_plus_1000).setOnClickListener {
            updateCategoryAmount(categoryId, view, 1000.0)
        }
    }

    private fun updateCategoryAmount(categoryId: Int, view: View, change: Double) {
        val currentAmount = categoryAmounts[categoryId] ?: 0.0
        val newAmount = currentAmount + change
        
        // Validation
        if (newAmount < 0) return 
        if (change > 0 && remainingBudget - change < 0) {
            android.widget.Toast.makeText(this, "Not enough budget left!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        categoryAmounts[categoryId] = newAmount
        remainingBudget -= change

        // Update target item view
        val tvAmount = view.findViewById<TextView>(R.id.tv_amount)
        val tvIncomePercentage = view.findViewById<TextView>(R.id.tv_income_percentage)

        tvAmount.text = "₹${newAmount.toInt()}"
        val percentage = (newAmount / totalIncome) * 100
        tvIncomePercentage.text = String.format("%.1f%% of income", percentage)

        updateHeaderDisplays()
    }

    private fun updateHeaderDisplays() {
        val tvRemainingBudget = findViewById<TextView>(R.id.tv_remaining_budget)
        val tvAssignedPercentage = findViewById<TextView>(R.id.tv_assigned_percentage)
        val progressBudget = findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progress_budget)

        tvRemainingBudget.text = "₹${remainingBudget.toInt()}"

        val allocatedAmount = totalIncome - remainingBudget
        val totalPercentage = (allocatedAmount / totalIncome) * 100
        tvAssignedPercentage.text = "${totalPercentage.toInt()}% assigned"
        progressBudget.progress = totalPercentage.toInt()
    }
}
