package com.simats.pavan_digi_wallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import java.util.Calendar
import com.google.android.material.card.MaterialCardView
import androidx.cardview.widget.CardView
import android.widget.GridLayout

class AddPlannedBillActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_planned_bill)

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val btnBack = findViewById<View>(R.id.btn_back)
        val etBillName = findViewById<EditText>(R.id.et_bill_name)
        val etAmount = findViewById<EditText>(R.id.et_amount)
        val etDueDate = findViewById<EditText>(R.id.et_due_date)
        val etRemindMe = findViewById<EditText>(R.id.et_remind_me)
        val btnSaveBill = findViewById<View>(R.id.btn_save_bill)
        val tvDateSummary = findViewById<TextView>(R.id.tv_date_summary)

        btnBack.setOnClickListener {
            finish()
        }

        val dateDay = intent.getStringExtra("EXTRA_DAY") ?: "4"
        val dateMonth = intent.getStringExtra("EXTRA_MONTH") ?: (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
        val dateYear = intent.getStringExtra("EXTRA_YEAR") ?: Calendar.getInstance().get(Calendar.YEAR).toString()
        
        etDueDate.setText(dateDay)
        tvDateSummary.text = "Due date set to ${dateDay}-${dateMonth}-${dateYear}. You can change specific day below."

        // Category selection logic
        val categoryGrid = findViewById<GridLayout>(R.id.category_grid) // I should add this ID to XML
        setupCategorySelection(etBillName)

        btnSaveBill.setOnClickListener {
            Toast.makeText(this, "Save Button Clicked!", Toast.LENGTH_SHORT).show()
            val name = etBillName.text.toString().trim()
            val amountStr = etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val day = etDueDate.text.toString().toIntOrNull() ?: 4
            val remindDays = etRemindMe.text.toString().toIntOrNull() ?: 3

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a bill name or select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construct full date string (YYYY-MM-DD)
            val monthInt = dateMonth.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
            val yearInt = dateYear.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val dateStr = String.format("%d-%02d-%02d", yearInt, monthInt, day)

            btnSaveBill.isEnabled = false // Prevent double clicks
            
            val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", 1)
            
            val request = ReminderRequest(
                userId = userId,
                billName = name,
                amount = amount,
                dueDate = dateStr,
                reminderDaysBefore = remindDays
            )

            RetrofitClient.apiService.addReminder(request).enqueue(object : retrofit2.Callback<ReminderResponse> {
                override fun onResponse(call: retrofit2.Call<ReminderResponse>, response: retrofit2.Response<ReminderResponse>) {
                    btnSaveBill.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddPlannedBillActivity, "Bill scheduled successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddPlannedBillActivity, "Error: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ReminderResponse>, t: Throwable) {
                    btnSaveBill.isEnabled = true
                    Toast.makeText(this@AddPlannedBillActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun setupCategorySelection(etBillName: EditText) {
        val categoryGrid = findViewById<GridLayout>(R.id.category_grid)
        
        for (i in 0 until categoryGrid.childCount) {
            val card = categoryGrid.getChildAt(i)
            card.setOnClickListener {
                // Reset all cards' highlight
                for (j in 0 until categoryGrid.childCount) {
                    val c = categoryGrid.getChildAt(j)
                    if (c is com.google.android.material.card.MaterialCardView) {
                        c.strokeWidth = 0
                    }
                }
                
                // Highlight this one
                if (card is com.google.android.material.card.MaterialCardView) {
                    card.strokeWidth = android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
                    ).toInt()
                    card.strokeColor = android.graphics.Color.parseColor("#3498DB")
                }
                
                // Get name from the TextView inside
                try {
                    val linearLayout = (card as? android.view.ViewGroup)?.getChildAt(0) as? android.view.ViewGroup
                    val textView = linearLayout?.getChildAt(1) as? TextView
                    textView?.let {
                        etBillName.setText(it.text.toString())
                    }
                } catch (e: Exception) {}
            }
        }
    }
}
