package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.ArrayList
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.simats.pavan_digi_wallet.PredictBalanceResponse
import com.simats.pavan_digi_wallet.AiInsightResponse
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var tvHomeAvgSpending: TextView
    private lateinit var tvTotalRemaining: TextView
    private lateinit var tvHomeTotalIncome: TextView
    private lateinit var tvSpendingRatio: TextView
    
    private var totalIncome: Double = 0.0
    private var totalExpense: Double = 0.0
    private var currentExpenses: Map<String, Double> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure Dark Mode is absolute
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Subscribe to global topic for easy testing via Python
        FirebaseMessaging.getInstance().subscribeToTopic("global")
        
        // Force Sync FCM Device Token!
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val sharedPreferences = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
                val userId = sharedPreferences.getInt("user_id", -1)
                if (userId != -1 && token != null) {
                    RetrofitClient.apiService.updateFcmToken(userId, FcmTokenRequest(token))
                        .enqueue(object : retrofit2.Callback<Map<String, Any>> {
                            override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {}
                            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {}
                        })
                }
            }
        }
        
        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        // Dark Theme Status Bar
        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBars.top, 0, 0)
                insets
            }
        }

        // Initialize UI components
        pieChart = findViewById(R.id.pie_chart_expense)
        tvHomeAvgSpending = findViewById(R.id.tv_home_avg_spending)
        tvTotalRemaining = findViewById(R.id.tv_total_remaining)
        tvHomeTotalIncome = findViewById(R.id.tv_home_total_income)
        tvSpendingRatio = findViewById(R.id.tv_spending_ratio)

        setupPieChart()
        loadExpenseData()

        // Navigation
        findViewById<View>(R.id.nav_activity)?.setOnClickListener { openHistory() }
        
        findViewById<View>(R.id.btn_menu)?.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, 0)
        }
        
        findViewById<View>(R.id.fab_add_main)?.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", "expense")
            startActivity(intent)
        }

        findViewById<View>(R.id.nav_ai_chat)?.setOnClickListener {
            startActivity(Intent(this, AiAssistantActivity::class.java))
        }

        findViewById<View>(R.id.card_zero_budget)?.setOnClickListener {
            startActivity(Intent(this, ZeroBasedBudgetActivity::class.java))
        }

        findViewById<View>(R.id.card_agentic_ai)?.setOnClickListener {
            startActivity(Intent(this, AgenticAiAnalysisActivity::class.java))
        }




        findViewById<View>(R.id.btn_home_ask_ai)?.setOnClickListener {
            startActivity(Intent(this, AiAssistantActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenseData() // Refresh chart data when returning to home
    }

    private fun setupPieChart() {
        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(12f, 12f, 12f, 12f)
            dragDecelerationFrictionCoef = 0.95f
            
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            
            holeRadius = 78f
            transparentCircleRadius = 82f
            
            setDrawCenterText(true)
            rotationAngle = -90f // Start from top
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            // Premium Bounce Animation
            animateY(1800, com.github.mikephil.charting.animation.Easing.EaseInOutQuart)
            
            legend.isEnabled = false
            
            // Clean look: Entry labels are hidden to avoid clutter/overlap
            setDrawEntryLabels(false) 
        }
    }

    private fun loadExpenseData() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)
        val userName = sharedPref.getString("user_name", "User")
        
        // Update welcome message
        findViewById<TextView>(R.id.tv_username)?.text = userName
        findViewById<TextView>(R.id.tv_profile_initials)?.text = userName?.take(2)?.uppercase() ?: "JD"

        // Fetch from Backend - Categorized for Pie Chart
        RetrofitClient.apiService.getExpenseCategorySummary(userId).enqueue(object : retrofit2.Callback<Map<String, Double>> {
            override fun onResponse(call: retrofit2.Call<Map<String, Double>>, response: retrofit2.Response<Map<String, Double>>) {
                if (response.isSuccessful && response.body() != null) {
                    val expenses = response.body()!!
                    currentExpenses = expenses
                    totalExpense = expenses.values.sum()
                    updateChart(expenses)
                    updateRemainingBalance()
                } else {
                    updateChart(mapOf())
                    updateRemainingBalance()
                }
            }
            override fun onFailure(call: retrofit2.Call<Map<String, Double>>, t: Throwable) {
                updateChart(mapOf())
                updateRemainingBalance()
            }
        })

        // Fetch Income Summary
        RetrofitClient.apiService.getIncomeSummary(userId).enqueue(object : retrofit2.Callback<Map<String, Double>> {
            override fun onResponse(call: retrofit2.Call<Map<String, Double>>, response: retrofit2.Response<Map<String, Double>>) {
                if (response.isSuccessful && response.body() != null) {
                    totalIncome = response.body()!!.values.sum()
                    tvHomeTotalIncome.text = "₹${String.format("%,.0f", totalIncome)}"
                    updateRemainingBalance()
                }
            }
            override fun onFailure(call: retrofit2.Call<Map<String, Double>>, t: Throwable) {}
        })


    }



    private fun useMockData() {
        val mockExpenses = mapOf(
            "Food" to 7500.0,
            "Rent" to 8000.0,
            "Shopping" to 2500.0,
            "Transport" to 1200.0,
            "Bills" to 800.0
        )
        updateChart(mockExpenses)
    }

    private fun updateChart(expenses: Map<String, Double>) {
        val entries = ArrayList<PieEntry>()
        var totalAmount = 0.0
        
        if (expenses.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Add an expense to see your breakdown")
            pieChart.invalidate()
            findViewById<TextView>(R.id.tv_total_expense)?.text = "₹0"
            return
        }

        for ((category, amount) in expenses) {
            if (amount > 0) {
                entries.add(PieEntry(amount.toFloat(), category))
                totalAmount += amount
            }
        }

        // Vibrant Modern Palette
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#4361EE")) // Royal Blue
        colors.add(Color.parseColor("#7209B7")) // Deep Purple
        colors.add(Color.parseColor("#F72585")) // Vivid Pink
        colors.add(Color.parseColor("#4CC9F0")) // Sky Blue
        colors.add(Color.parseColor("#4895EF")) // Soft Blue
        colors.add(Color.parseColor("#3F37C9")) // Navy Blue

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.sliceSpace = 4f // Adds gap between slices for a cleaner look
        dataSet.selectionShift = 12f // Slices pop out more when clicked
        
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 13f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTypeface = Typeface.DEFAULT_BOLD

        val data = PieData(dataSet)
        data.setValueFormatter(com.github.mikephil.charting.formatter.PercentFormatter(pieChart))
        
        pieChart.data = data
        
        // Advanced Center Text (Dynamically formatted)
        val amountStr = "₹${String.format("%,.0f", totalAmount)}"
        val fullText = "TOTAL\n$amountStr"
        val s = SpannableString(fullText)
        
        // Style "TOTAL" part
        s.setSpan(ForegroundColorSpan(Color.parseColor("#9A9FA5")), 0, 5, 0)
        s.setSpan(RelativeSizeSpan(0.8f), 0, 5, 0)
        
        // Style visual amount part
        s.setSpan(ForegroundColorSpan(Color.WHITE), 6, fullText.length, 0)
        s.setSpan(RelativeSizeSpan(1.7f), 6, fullText.length, 0)
        s.setSpan(StyleSpan(Typeface.BOLD), 6, fullText.length, 0)
        
        pieChart.centerText = s
        pieChart.highlightValues(null) // Reset highlights
        pieChart.invalidate()

        // Update Total Spend TextView
        findViewById<TextView>(R.id.tv_total_expense)?.text = "₹${String.format("%,.2f", totalAmount)}"
    }

    private fun updateRemainingBalance() {
        val remaining = totalIncome - totalExpense
        tvTotalRemaining.text = "₹${String.format("%,.0f", remaining)}"
        
        if (remaining < 0) {
            tvTotalRemaining.setTextColor(Color.parseColor("#E74C3C")) // Red
        } else {
            tvTotalRemaining.setTextColor(Color.WHITE)
        }
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }
}
