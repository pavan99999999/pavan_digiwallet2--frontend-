package com.simats.pavan_digi_wallet

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class AddLiabilityActivity : AppCompatActivity() {

    private lateinit var etBalance: EditText
    private lateinit var etLiabilityName: EditText
    private var balanceString = ""
    private var selectedCategory = "Personal Loan"
    
    private lateinit var catContainers: List<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_liability)

        // Dark status bar for dark theme
        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        etBalance = findViewById(R.id.et_balance)
        etLiabilityName = findViewById(R.id.et_liability_name)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        setupCategories()
        setupKeypad()

        val saveAction = View.OnClickListener {
            val name = etLiabilityName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter liability name", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (balanceString.isNotEmpty()) {
                val amount = balanceString.toDoubleOrNull() ?: 0.0
                val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 1)

                val request = LiabilityRequest(
                    userId = userId,
                    category = selectedCategory,
                    liabilityName = name,
                    amount = amount
                )

                RetrofitClient.apiService.addLiability(request).enqueue(object : retrofit2.Callback<Map<String, Any>> {
                    override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddLiabilityActivity, "Liability added: $name", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddLiabilityActivity, "Failed to save liability", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                        Toast.makeText(this@AddLiabilityActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btn_save_top).setOnClickListener(saveAction)
        findViewById<View>(R.id.btn_add_liability).setOnClickListener(saveAction)
    }

    private fun setupCategories() {
        catContainers = listOf(
            findViewById(R.id.cat_personal_loan),
            findViewById(R.id.cat_credit_card),
            findViewById(R.id.cat_home_loan),
            findViewById(R.id.cat_vehicle_loan),
            findViewById(R.id.cat_other_debt)
        )

        catContainers.forEach { container ->
            container.setOnClickListener {
                selectCategory(container)
            }
        }
    }

    private fun selectCategory(selected: LinearLayout) {
        // Reset all
        catContainers.forEach {
            it.setBackgroundResource(R.drawable.bg_input_dark)
            val tv = it.getChildAt(1) as? TextView
            tv?.setTextColor(Color.WHITE)
            tv?.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        // Highlight selected
        selected.setBackgroundResource(R.drawable.bg_outline_red)
        val tvSelected = selected.getChildAt(1) as? TextView
        tvSelected?.setTextColor(Color.parseColor("#FF2B7A"))
        tvSelected?.setTypeface(null, android.graphics.Typeface.BOLD)
        
        selectedCategory = tvSelected?.text?.toString() ?: ""
    }

    private fun setupKeypad() {
        val keys = listOf(
            R.id.key_1 to "1", R.id.key_2 to "2", R.id.key_3 to "3",
            R.id.key_4 to "4", R.id.key_5 to "5", R.id.key_6 to "6",
            R.id.key_7 to "7", R.id.key_8 to "8", R.id.key_9 to "9",
            R.id.key_0 to "0"
        )

        keys.forEach { (id, value) ->
            val keyView = findViewById<View>(id)
            if (keyView != null) {
                keyView.findViewById<TextView>(R.id.tv_key).text = value
                keyView.setOnClickListener { onKeyPressed(value) }
            }
        }

        findViewById<View>(R.id.key_backspace)?.setOnClickListener { onBackspace() }
    }

    private fun onKeyPressed(value: String) {
        balanceString += value
        updateBalanceDisplay()
    }

    private fun onBackspace() {
        if (balanceString.isNotEmpty()) {
            balanceString = balanceString.substring(0, balanceString.length - 1)
            updateBalanceDisplay()
        }
    }

    private fun updateBalanceDisplay() {
        etBalance.setText("₹ $balanceString")
    }
}
