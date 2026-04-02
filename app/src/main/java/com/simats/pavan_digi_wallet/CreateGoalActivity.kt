package com.simats.pavan_digi_wallet

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class CreateGoalActivity : AppCompatActivity() {

    private var selectedBackendDate: String = ""
    private var selectedDeadlineCalendar: Calendar? = null
    
    private lateinit var etGoalName: EditText
    private lateinit var etTargetAmount: EditText
    private lateinit var etSavedAmount: EditText
    private lateinit var etTargetDate: EditText
    private lateinit var tvMonthlyPaymentLabel: TextView
    private var calculatedMonthlyPayment: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_goal)

        // Set status bar to light theme
        window.statusBarColor = android.graphics.Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        etGoalName = findViewById(R.id.et_goal_name)
        etTargetAmount = findViewById(R.id.et_target_amount)
        etSavedAmount = findViewById(R.id.et_saved_amount)
        etTargetDate = findViewById(R.id.et_target_date)
        tvMonthlyPaymentLabel = findViewById<TextView>(R.id.tv_monthly_payment_label)
        val btnCreateGoal = findViewById<Button>(R.id.btn_create_goal)

        etTargetDate.setOnClickListener {
            showDatePicker(etTargetDate)
        }

        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateMonthlySaving()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        etTargetAmount.addTextChangedListener(textWatcher)
        etSavedAmount.addTextChangedListener(textWatcher)

        btnCreateGoal.setOnClickListener {
            val name = etGoalName.text.toString().trim()
            val amountStr = etTargetAmount.text.toString().trim()
            val savedStr = etSavedAmount.text.toString().trim()
            val date = selectedBackendDate

            if (name.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val saved = savedStr.toDoubleOrNull() ?: 0.0
            createGoal(name, amount, saved, date, calculatedMonthlyPayment)
        }
    }

    private fun calculateMonthlySaving() {
        val target = etTargetAmount.text.toString().toDoubleOrNull() ?: 0.0
        val saved = etSavedAmount.text.toString().toDoubleOrNull() ?: 0.0
        val remaining = target - saved

        val deadline = selectedDeadlineCalendar
        if (deadline == null || remaining <= 0) {
            tvMonthlyPaymentLabel.text = "₹0.00"
            calculatedMonthlyPayment = 0.0
            return
        }

        val today = Calendar.getInstance()
        val yearDiff = deadline.get(Calendar.YEAR) - today.get(Calendar.YEAR)
        val monthDiff = deadline.get(Calendar.MONTH) - today.get(Calendar.MONTH)
        val totalMonths = (yearDiff * 12) + monthDiff

        val monthsCount = if (totalMonths <= 0) 1 else totalMonths
        calculatedMonthlyPayment = remaining / monthsCount
        tvMonthlyPaymentLabel.text = String.format("₹%.2f", calculatedMonthlyPayment)
    }

    private fun createGoal(name: String, amount: Double, saved: Double, date: String, monthlyPayment: Double) {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        val request = GoalRequest(userId, name, amount, saved, date, monthlyPayment)

        RetrofitClient.apiService.createGoal(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateGoalActivity, "Goal Created 🎯", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateGoalActivity, "Failed to create goal", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@CreateGoalActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val displayDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                selectedBackendDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                
                selectedDeadlineCalendar = Calendar.getInstance()
                selectedDeadlineCalendar?.set(selectedYear, selectedMonth, selectedDay)
                
                editText.setText(displayDate)
                calculateMonthlySaving()
            },
            year,
            month,
            day
        )

        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.MONTH, 1) // Min 1 month in future
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        datePickerDialog.show()
    }
}
