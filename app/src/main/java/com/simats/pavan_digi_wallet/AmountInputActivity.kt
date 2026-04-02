package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Calendar
import java.util.Locale
import java.util.Date
import android.app.DatePickerDialog
import java.text.SimpleDateFormat

class AmountInputActivity : AppCompatActivity() {

    private lateinit var etAmount: android.widget.EditText
    private lateinit var btnSave: TextView
    private lateinit var imgCategory: ImageView
    private lateinit var tvDate: TextView
    private lateinit var selectedDate: Calendar
    private val serverDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val uiDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_amount_input)

        // Dark status bar
        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        val categoryName = intent.getStringExtra("category") ?: "Other"
        val iconRes = intent.getIntExtra("icon_res", R.drawable.ic_briefcase)
        val transactionType = intent.getStringExtra("type") ?: "expense"

        // Set header title & category label
        findViewById<TextView>(R.id.tv_title).text = categoryName
        findViewById<TextView>(R.id.tv_category_label).text = categoryName

        etAmount = findViewById(R.id.et_amount)
        btnSave = findViewById(R.id.btn_save)
        imgCategory = findViewById(R.id.img_category)

        // Tint the category icon based on transaction type
        val iconTint = if (transactionType == "income") Color.parseColor("#2ECC71")
                       else Color.parseColor("#3498DB")
        imgCategory.setImageResource(iconRes)
        imgCategory.setColorFilter(iconTint)

        findViewById<View>(R.id.btn_back).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Date Selection Init
        tvDate = findViewById(R.id.tv_date)
        selectedDate = Calendar.getInstance()
        tvDate.text = uiDateFormat.format(selectedDate.time)
        
        findViewById<View>(R.id.ll_date_picker).setOnClickListener {
            showDatePicker()
        }

        // Automatically show system keyboard with a small delay for reliability
        etAmount.postDelayed({
            etAmount.requestFocus()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(etAmount, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        // Handle 'Done' action from mobile keyboard
        etAmount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                if (btnSave.isEnabled) {
                    btnSave.performClick()
                }
                true
            } else false
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString()
            if (amountStr.isNotEmpty() && amountStr != "0" && amountStr != "0.") {
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                val formattedType = transactionType.uppercase()
                val apiDate = serverDateFormat.format(selectedDate.time)

                // Show loading state
                btnSave.isEnabled = false
                btnSave.text = "Saving…"

                val request = TransactionRequest(
                    userId = userId,
                    amount = amount,
                    category = categoryName,
                    transactionType = formattedType,
                    date = apiDate
                )

                RetrofitClient.apiService.addTransaction(request).enqueue(object : retrofit2.Callback<TransactionResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<TransactionResponse>,
                        response: retrofit2.Response<TransactionResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AmountInputActivity, "Transaction Saved!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AmountInputActivity, HistoryActivity::class.java)
                            intent.putExtra("open_tab", transactionType.lowercase()) // "income" or "expense"
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        } else {
                            btnSave.isEnabled = true
                            btnSave.text = "Done"
                            Toast.makeText(this@AmountInputActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<TransactionResponse>, t: Throwable) {
                        btnSave.isEnabled = true
                        btnSave.text = "Done"
                        Toast.makeText(this@AmountInputActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDisplay(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Select all text on focus for easy replacement
        // Removed to allow cursor placement
        
        updateDisplay("")
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                tvDate.text = uiDateFormat.format(selectedDate.time)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDisplay(amount: String) {
        val hasAmount = amount.isNotEmpty() && amount != "0"
        
        val hasValidAmount = hasAmount && (amount.toDoubleOrNull() ?: 0.0) > 0
        btnSave.isEnabled = hasValidAmount
        
        val alpha = if (hasValidAmount) 1f else 0.4f
        btnSave.alpha = alpha
        findViewById<View>(R.id.card_save)?.alpha = alpha
    }
}
