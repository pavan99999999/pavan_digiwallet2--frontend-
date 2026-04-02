package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var tvMonthYear: TextView
    private lateinit var layoutTransactions: LinearLayout
    private lateinit var layoutEmptyState: View
    private lateinit var tvMonthIncome: TextView
    private lateinit var tvMonthExpenses: TextView
    private lateinit var tvMonthTotal: TextView

    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        window.statusBarColor = Color.parseColor("#1A222E")
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        tvMonthYear = findViewById(R.id.tv_month_year)
        layoutTransactions = findViewById(R.id.layout_transactions)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        tvMonthIncome = findViewById(R.id.tv_month_summary_income)
        tvMonthExpenses = findViewById(R.id.tv_month_summary_expenses)
        tvMonthTotal = findViewById(R.id.tv_month_summary_total)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_prev_month).setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            updateMonthView()
        }
        findViewById<ImageView>(R.id.btn_next_month).setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            updateMonthView()
        }

        findViewById<View>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.nav_activity).setOnClickListener {
            // Self click
        }

        findViewById<View>(R.id.nav_ai_chat).setOnClickListener {
            startActivity(Intent(this, AiAssistantActivity::class.java))
        }

        findViewById<View>(R.id.fab_add_main).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        updateMonthView()
    }

    override fun onResume() {
        super.onResume()
        fetchMonthTransactions()
    }

    private fun updateMonthView() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
        tvMonthYear.text = selectedDate.format(formatter)

        fetchMonthTransactions()
    }

    private fun fetchMonthTransactions() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        RetrofitClient.apiService.getHistory(userId).enqueue(object : Callback<List<TransactionData>> {
            override fun onResponse(call: Call<List<TransactionData>>, response: Response<List<TransactionData>>) {
                if (response.isSuccessful) {
                    val allTxns = response.body() ?: emptyList()
                    val monthTxns = allTxns.filter {
                        try {
                            val date = LocalDate.parse(it.date)
                            date.month == selectedDate.month && date.year == selectedDate.year
                        } catch (e: Exception) { false }
                    }.sortedByDescending { it.date }

                    displayTransactions(monthTxns)
                    updateSummary(monthTxns)
                } else {
                    Toast.makeText(this@HistoryActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TransactionData>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayTransactions(transactions: List<TransactionData>) {
        layoutTransactions.removeAllViews()

        if (transactions.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            layoutTransactions.visibility = View.GONE
            return
        }

        layoutEmptyState.visibility = View.GONE
        layoutTransactions.visibility = View.VISIBLE

        val grouped = transactions.groupBy { it.date }
        
        for ((dateStr, txns) in grouped) {
            val date = LocalDate.parse(dateStr)
            
            // Day Header
            val header = LayoutInflater.from(this).inflate(R.layout.item_transaction_header, layoutTransactions, false)
            header.findViewById<TextView>(R.id.tv_day_num).text = date.dayOfMonth.toString()
            header.findViewById<TextView>(R.id.tv_day_name).text = date.dayOfWeek.name.take(3)
                .lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            header.findViewById<TextView>(R.id.tv_full_date).text = date.format(DateTimeFormatter.ofPattern("MM.yyyy"))
            
            val dayIncome = txns.filter { it.transactionType.equals("income", ignoreCase = true) }.sumOf { it.amount }
            val dayExpense = txns.filter { it.transactionType.equals("expense", ignoreCase = true) }.sumOf { it.amount }
            
            header.findViewById<TextView>(R.id.tv_day_income).text = String.format("₹%.0f", dayIncome)
            header.findViewById<TextView>(R.id.tv_day_expenses).text = String.format("₹%.0f", dayExpense)
            
            layoutTransactions.addView(header)
            
            // Transaction Rows
            for (txn in txns) {
                val row = LayoutInflater.from(this).inflate(R.layout.item_transaction_row, layoutTransactions, false)
                row.findViewById<TextView>(R.id.tv_category).text = txn.category
                row.findViewById<TextView>(R.id.tv_account).text = txn.note ?: "Accounts"
                row.findViewById<TextView>(R.id.tv_amount).text = String.format("₹%.2f", txn.amount)
                
                val color = if (txn.transactionType.equals("income", ignoreCase = true)) Color.parseColor("#3498DB") else Color.parseColor("#E67E22")
                row.findViewById<TextView>(R.id.tv_amount).setTextColor(color)

                val iconRes = getIconForCategory(txn.category)
                row.findViewById<ImageView>(R.id.img_cat).setImageResource(iconRes)

                row.setOnLongClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete Transaction")
                        .setMessage("Delete this entry?")
                        .setPositiveButton("Delete") { _, _ -> deleteTransaction(txn.id) }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                
                layoutTransactions.addView(row)
            }
        }
    }

    private fun updateSummary(transactions: List<TransactionData>) {
        val income = transactions.filter { it.transactionType.equals("income", ignoreCase = true) }.sumOf { it.amount }
        val expenses = transactions.filter { it.transactionType.equals("expense", ignoreCase = true) }.sumOf { it.amount }
        val total = income - expenses

        tvMonthIncome.text = String.format("₹%.0f", income)
        tvMonthExpenses.text = String.format("₹%.0f", expenses)
        tvMonthTotal.text = String.format("₹%.0f", total)
    }

    private fun deleteTransaction(id: Int) {
        RetrofitClient.apiService.deleteTransaction(id).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@HistoryActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    fetchMonthTransactions()
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
        })
    }

    private fun getIconForCategory(category: String): Int {
        return when (category.lowercase()) {
            "food", "dining" -> R.drawable.ic_food_fork
            "transport", "taxi" -> R.drawable.ic_car_alt
            "shopping" -> R.drawable.ic_cart
            "bills", "utilities" -> R.drawable.ic_document
            "salary", "income" -> R.drawable.ic_money_bag
            "health" -> R.drawable.ic_health
            "education" -> R.drawable.ic_education
            "entertainment" -> R.drawable.ic_music
            else -> R.drawable.ic_activity
        }
    }
}
