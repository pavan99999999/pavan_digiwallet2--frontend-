package com.simats.pavan_digi_wallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmartPurchasePlannerActivity : AppCompatActivity() {

    private lateinit var goalAdapter: GoalAdapter
    private var allGoals: List<GoalData> = emptyList()
    private var isAmountView = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_smart_purchase_planner)

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_add_goal).setOnClickListener {
            val intent = Intent(this, CreateGoalActivity::class.java)
            startActivity(intent)
        }

        // Top Navigation Tabs
        val btnBudgets = findViewById<View>(R.id.tv_tab_budgets)
        val btnCalculators = findViewById<View>(R.id.tv_tab_calculators)
        val btnLearn = findViewById<View>(R.id.tv_tab_learn)

        btnLearn.setOnClickListener {
            startActivity(Intent(this, FinancialTipsActivity::class.java))
        }

        // Toggle Tabs (for Chart)
        val tabAmount = findViewById<TextView>(R.id.tv_tab_amount)
        val tabTime = findViewById<TextView>(R.id.tv_tab_time)

        tabAmount.setOnClickListener {
            if (!isAmountView) {
                isAmountView = true
                updateTabStyles(tabAmount, tabTime)
                updateChart(allGoals)
            }
        }

        tabTime.setOnClickListener {
            if (isAmountView) {
                isAmountView = false
                updateTabStyles(tabTime, tabAmount)
                updateChart(allGoals)
            }
        }

        setupRecyclerView()
    }

    private fun updateTabStyles(active: TextView, inactive: TextView) {
        active.setBackgroundResource(R.drawable.bg_tab_pill)
        active.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3498DB"))
        active.setTextColor(android.graphics.Color.WHITE)
        
        inactive.backgroundTintList = null
        inactive.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        inactive.setTextColor(android.graphics.Color.parseColor("#6F767E"))
    }

    override fun onResume() {
        super.onResume()
        fetchGoals()
    }

    private fun setupRecyclerView() {
        val rvGoals = findViewById<RecyclerView>(R.id.rv_goals)
        rvGoals.layoutManager = LinearLayoutManager(this)
        goalAdapter = GoalAdapter(emptyList()) { goal ->
            Toast.makeText(this, "Selected: ${goal.goalName} (${goal.progressPercentage.toInt()}%)", Toast.LENGTH_SHORT).show()
        }
        rvGoals.adapter = goalAdapter
    }

    private fun fetchGoals() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        RetrofitClient.apiService.getGoals(userId).enqueue(object : Callback<List<GoalData>> {
            override fun onResponse(call: Call<List<GoalData>>, response: Response<List<GoalData>>) {
                if (response.isSuccessful) {
                    allGoals = response.body() ?: emptyList()
                    goalAdapter.updateData(allGoals)
                    updateChart(allGoals)
                } else {
                    Log.e("API_ERROR", "Failed to fetch goals: ${response.message()}")
                    Toast.makeText(this@SmartPurchasePlannerActivity, "Failed to load goals", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<GoalData>>, t: Throwable) {
                Log.e("API_FAILURE", "Error fetching goals", t)
                Toast.makeText(this@SmartPurchasePlannerActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateChart(goals: List<GoalData>) {
        val barChart = findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.bar_chart_goals)
        
        var totalSaved = 0f
        var totalTarget = 0f
        var totalDays = 0
        
        for (goal in goals) {
            totalSaved += goal.savedAmount.toFloat()
            totalTarget += goal.targetAmount.toFloat()
            totalDays += goal.daysRemaining
        }

        val avgDays = if (goals.isNotEmpty()) totalDays / goals.size else 0
        
        findViewById<TextView>(R.id.tv_total_saved_overall)?.text = "₹${String.format("%,.0f", totalSaved)}"
        findViewById<TextView>(R.id.tv_total_target_overall)?.text = "₹${String.format("%,.0f", totalTarget)}"
        findViewById<TextView>(R.id.tv_avg_time_overall)?.text = "$avgDays Days"

        val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
        val labels = ArrayList<String>()
        
        if (isAmountView) {
            // Aggregate Saved vs Target
            entries.add(com.github.mikephil.charting.data.BarEntry(0f, totalSaved))
            entries.add(com.github.mikephil.charting.data.BarEntry(1f, totalTarget))
            labels.add("Saved")
            labels.add("Target")
        } else {
            // Time remaining per Goal
            goals.take(6).forEachIndexed { index, goal ->
                entries.add(com.github.mikephil.charting.data.BarEntry(index.toFloat(), goal.daysRemaining.toFloat()))
                labels.add(if (goal.goalName.length > 8) goal.goalName.take(6) + ".." else goal.goalName)
            }
        }

        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, if (isAmountView) "Finance Summary" else "Days Left")
        dataSet.colors = if (isAmountView) {
            listOf(android.graphics.Color.parseColor("#4CC9F0"), android.graphics.Color.parseColor("#F72585"))
        } else {
            listOf(android.graphics.Color.parseColor("#4361EE"))
        }
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.valueTextSize = 10f

        val barData = com.github.mikephil.charting.data.BarData(dataSet)
        if (!isAmountView) barData.barWidth = 0.4f
        
        barChart.data = barData
        
        // Styling
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.xAxis.textColor = android.graphics.Color.WHITE
        barChart.axisLeft.textColor = android.graphics.Color.WHITE
        barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        
        barChart.animateY(800)
        barChart.invalidate()
    }
}
