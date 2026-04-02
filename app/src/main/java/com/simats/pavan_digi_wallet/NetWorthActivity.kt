package com.simats.pavan_digi_wallet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.ArrayList

class NetWorthActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var layoutAssets: LinearLayout
    private lateinit var layoutLiabilities: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_net_worth)

        // Set status bar to dark (matches background #0F1520)
        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        barChart = findViewById(R.id.bar_chart)
        layoutAssets = findViewById(R.id.layout_assets_list)
        layoutLiabilities = findViewById(R.id.layout_liabilities_list)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_add_asset).setOnClickListener {
            val intent = android.content.Intent(this, AddAssetActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btn_add_liability).setOnClickListener {
            val intent = android.content.Intent(this, AddLiabilityActivity::class.java)
            startActivity(intent)
        }

        setupChart()
        populateDetails()
    }

    private fun setupChart() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setTouchEnabled(false)

        // X-Axis configurations
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val labels = listOf("Assets", "Liabilities")
                return labels.getOrNull(value.toInt()) ?: ""
            }
        }
        xAxis.granularity = 1f
        xAxis.labelCount = 2

        // Y-Axis configurations
        barChart.axisLeft.apply {
            textColor = Color.parseColor("#9A9FA5")
            setDrawGridLines(true)
            gridColor = Color.parseColor("#1F2936")
            axisLineColor = Color.TRANSPARENT
        }
        barChart.axisRight.isEnabled = false
        
        barChart.animateY(1000)
    }

    private var totalAssets = 0.0
    private var totalLiabilities = 0.0

    override fun onResume() {
        super.onResume()
        populateDetails()
    }

    private fun populateDetails() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        totalAssets = 0.0
        totalLiabilities = 0.0
        
        layoutAssets.removeAllViews()
        layoutLiabilities.removeAllViews()

        // Fetch Assets from API
        RetrofitClient.apiService.getAssets(userId).enqueue(object : retrofit2.Callback<List<AssetData>> {
            override fun onResponse(call: retrofit2.Call<List<AssetData>>, response: retrofit2.Response<List<AssetData>>) {
                if (response.isSuccessful && response.body() != null) {
                    val assets = response.body()!!
                    assets.forEach { asset ->
                        totalAssets += asset.amount
                        addCategoryItem(layoutAssets, asset.assetName, asset.amount, asset.category)
                    }
                    updateSummary()
                }
            }
            override fun onFailure(call: retrofit2.Call<List<AssetData>>, t: Throwable) {}
        })

        // Fetch Liabilities from API
        RetrofitClient.apiService.getLiabilities(userId).enqueue(object : retrofit2.Callback<List<LiabilityData>> {
            override fun onResponse(call: retrofit2.Call<List<LiabilityData>>, response: retrofit2.Response<List<LiabilityData>>) {
                if (response.isSuccessful && response.body() != null) {
                    val liabilities = response.body()!!
                    liabilities.forEach { liability ->
                        totalLiabilities += liability.amount
                        addCategoryItem(layoutLiabilities, liability.liabilityName, liability.amount, liability.category)
                    }
                    updateSummary()
                }
            }
            override fun onFailure(call: retrofit2.Call<List<LiabilityData>>, t: Throwable) {}
        })
    }

    private fun addCategoryItem(layout: LinearLayout, name: String, amount: Double, category: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_category, layout, false)
        
        // Apply dark theme to the card
        val card = view as? androidx.cardview.widget.CardView
        card?.setCardBackgroundColor(Color.parseColor("#171E2B"))
        
        view.findViewById<TextView>(R.id.tv_cat_name).apply {
            text = name
            setTextColor(Color.WHITE)
        }
        view.findViewById<TextView>(R.id.tv_cat_amount).apply {
            text = "₹${String.format("%,.0f", amount)}"
            setTextColor(if (layout == layoutAssets) Color.parseColor("#00D09E") else Color.parseColor("#FF2B7A"))
        }
        view.findViewById<TextView>(R.id.tv_cat_percent).apply {
            text = category
            setTextColor(Color.parseColor("#9A9FA5"))
        }
        
        layout.addView(view)
    }

    private fun updateSummary() {
        val tvTotalAssets = findViewById<TextView>(R.id.tv_total_assets)
        val tvTotalLiabilities = findViewById<TextView>(R.id.tv_total_liabilities)
        val tvNetWorth = findViewById<TextView>(R.id.tv_net_worth)

        tvTotalAssets?.text = "₹${String.format("%,.0f", totalAssets)}"
        tvTotalLiabilities?.text = "₹${String.format("%,.0f", totalLiabilities)}"
        
        val netWorth = totalAssets - totalLiabilities
        tvNetWorth?.text = "₹${String.format("%,.0f", netWorth)}"

        updateBarChart()
    }

    private fun updateBarChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, totalAssets.toFloat()))
        entries.add(BarEntry(1f, totalLiabilities.toFloat()))

        val dataSet = BarDataSet(entries, "Comparison")
        dataSet.colors = listOf(Color.parseColor("#00D09E"), Color.parseColor("#FF2B7A"))
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        barChart.data = data
        barChart.invalidate()
    }
}
