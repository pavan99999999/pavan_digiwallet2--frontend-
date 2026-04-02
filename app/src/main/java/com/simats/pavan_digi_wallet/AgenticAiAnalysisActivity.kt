package com.simats.pavan_digi_wallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgenticAiAnalysisActivity : AppCompatActivity() {

    private lateinit var etIncome: EditText
    private lateinit var etFood: EditText
    private lateinit var etShopping: EditText
    private lateinit var etTransport: EditText
    private lateinit var etQuestion: EditText
    private lateinit var btnAnalyze: Button
    private lateinit var layoutResults: LinearLayout
    private lateinit var tvAiAdvice: TextView
    private lateinit var tvStatsSummary: TextView
    private lateinit var btnDownloadPdf: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView

    private var pdfUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agentic_ai_analysis)

        // Initialize Views
        etIncome = findViewById(R.id.et_income)
        etFood = findViewById(R.id.et_food)
        etShopping = findViewById(R.id.et_shopping)
        etTransport = findViewById(R.id.et_transport)
        etQuestion = findViewById(R.id.et_question)
        btnAnalyze = findViewById(R.id.btn_analyze)
        layoutResults = findViewById(R.id.layout_results)
        tvAiAdvice = findViewById(R.id.tv_ai_advice)
        tvStatsSummary = findViewById(R.id.tv_stats_summary)
        btnDownloadPdf = findViewById(R.id.btn_download_pdf)
        progressBar = findViewById(R.id.progress_bar)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener { finish() }

        btnAnalyze.setOnClickListener {
            performAnalysis()
        }

        btnDownloadPdf.setOnClickListener {
            pdfUrl?.let { url ->
                val baseUrl = RetrofitClient.getBaseUrl()
                val finalUrl = if (url.startsWith("http")) url else baseUrl + url
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                startActivity(intent)
            }
        }

        val btnAutoAnalyze = findViewById<Button>(R.id.btn_auto_analyze)
        btnAutoAnalyze.setOnClickListener {
            performAutoAnalysis()
        }
    }

    private fun performAutoAnalysis() {
        val sharedPref = getSharedPreferences("digi_wallet", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        progressBar.visibility = View.VISIBLE
        btnAnalyze.isEnabled = false
        val btnAuto = findViewById<Button>(R.id.btn_auto_analyze)
        btnAuto.isEnabled = false

        RetrofitClient.apiService.getInsights(userId).enqueue(object : Callback<InsightsResponse> {
            override fun onResponse(call: Call<InsightsResponse>, response: Response<InsightsResponse>) {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true
                btnAuto.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    displayAutoResults(data)
                } else {
                    Toast.makeText(this@AgenticAiAnalysisActivity, "Analysis failed", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<InsightsResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true
                btnAuto.isEnabled = true
                Toast.makeText(this@AgenticAiAnalysisActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayAutoResults(data: InsightsResponse) {
        layoutResults.visibility = View.VISIBLE
        tvAiAdvice.text = data.aiAdvice
        
        val summary = data.analysis.summary
        val subs = data.analysis.subscriptions
        val planned = data.analysis.plannedPayments
        
        val statsText = StringBuilder()
        statsText.append("Income: ₹${summary.income}\n")
        statsText.append("Expense: ₹${summary.expense}\n")
        statsText.append("Savings: ₹${summary.savings}\n\n")
        
        if (subs.isNotEmpty()) {
            statsText.append("Detected Subscriptions:\n")
            subs.forEach { statsText.append("- $it\n") }
            statsText.append("\n")
        }
        
        if (planned.isNotEmpty()) {
            statsText.append("Planned Payment Days: ")
            statsText.append(planned.joinToString(", "))
            statsText.append("\n\n")
        }

        data.analysis.monthComparison?.takeIf { it.isNotEmpty() }?.let { statsText.append("📊 Month vs Month:\n$it\n\n") }
        data.analysis.smartChanges?.takeIf { it.isNotEmpty() }?.let { statsText.append("📈 Category Changes:\n$it\n\n") }
        data.analysis.predictedBalance?.takeIf { it.isNotEmpty() }?.let { statsText.append("🔮 Future Forecast:\n$it\n\n") }
        data.analysis.goalPrediction?.takeIf { it.isNotEmpty() }?.let { statsText.append("🎯 Goal Tracking:\n$it\n\n") }
        data.analysis.smartTips?.takeIf { it.isNotEmpty() }?.let { statsText.append("💡 Smart AI Tips:\n$it\n\n") }
        data.analysis.financialHealth?.takeIf { it.isNotEmpty() }?.let { statsText.append("🏥 Financial Health Trim:\n$it\n\n") }
        
        // PRO MAX Features
        data.analysis.financialScore?.takeIf { it.isNotEmpty() }?.let { statsText.append("💯 Financial Score:\n$it\n\n") }
        data.analysis.behavior?.takeIf { it.isNotEmpty() }?.let { statsText.append("🧠 Behavior Analysis:\n$it\n\n") }
        data.analysis.budgetPlan?.takeIf { it.isNotEmpty() }?.let { statsText.append("📊 Recommended Budget:\n$it\n\n") }
        data.analysis.riskAlerts?.takeIf { it.isNotEmpty() }?.let { statsText.append("⚠️ Risk Alerts:\n$it\n\n") }
        data.analysis.personality?.takeIf { it.isNotEmpty() }?.let { statsText.append("👤 Financial Personality:\n$it\n\n") }
        data.analysis.anomalies?.takeIf { it.isNotEmpty() }?.let { statsText.append("🔍 Anomalies Detected:\n$it\n\n") }

        tvStatsSummary.text = statsText.toString().trim()
        
        val prefs = getSharedPreferences("digi_wallet", MODE_PRIVATE)
        val uid = prefs.getInt("user_id", 1)
        pdfUrl = "download-enhanced-report/$uid"
        btnDownloadPdf.visibility = View.VISIBLE
        
        // Scroll to results
        layoutResults.parent.requestChildFocus(layoutResults, layoutResults)
    }

    private fun performAnalysis() {
        val income = etIncome.text.toString().toDoubleOrNull() ?: 0.0
        val food = etFood.text.toString().toDoubleOrNull() ?: 0.0
        val shopping = etShopping.text.toString().toDoubleOrNull() ?: 0.0
        val transport = etTransport.text.toString().toDoubleOrNull() ?: 0.0
        val question = etQuestion.text.toString()

        if (income <= 0 || question.isEmpty()) {
            Toast.makeText(this, "Please enter income and a question", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnAnalyze.isEnabled = false

        val request = FinanceAnalysisRequest(income, food, shopping, transport, question)

        RetrofitClient.apiService.analyzeFinance(request).enqueue(object : Callback<FinanceAnalysisResponse> {
            override fun onResponse(call: Call<FinanceAnalysisResponse>, response: Response<FinanceAnalysisResponse>) {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    displayResults(data)
                } else {
                    Toast.makeText(this@AgenticAiAnalysisActivity, "Analysis failed: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FinanceAnalysisResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true
                Toast.makeText(this@AgenticAiAnalysisActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayResults(data: FinanceAnalysisResponse) {
        layoutResults.visibility = View.VISIBLE
        tvAiAdvice.text = data.aiAdvice
        
        val analysis = data.analysis
        val health = analysis["health"] as? Map<String, Any>
        val healthText = if (health != null) {
            "Expense Ratio: ${health["expense_ratio"]}%\nSavings Ratio: ${health["savings_ratio"]}%"
        } else {
            "Statistical summary successfully generated."
        }
        
        tvStatsSummary.text = healthText
        pdfUrl = data.pdfReportUrl
        
        // Scroll to results
        layoutResults.parent.requestChildFocus(layoutResults, layoutResults)
    }
}
