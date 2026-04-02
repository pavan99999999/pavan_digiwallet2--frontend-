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

class AddAssetActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etAssetName: EditText
    private var selectedCategory = "Bank Account"
    
    private lateinit var catContainers: List<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_asset)

        // Dark status bar for dark theme
        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        etAmount = findViewById(R.id.et_amount)
        etAssetName = findViewById(R.id.et_asset_name)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        
        setupCategories()

        val saveAction = View.OnClickListener {
            val assetName = etAssetName.text.toString().trim()
            if (assetName.isEmpty()) {
                Toast.makeText(this, "Please enter asset name", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val amountStr = etAmount.text.toString().trim()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 1)

                val request = AssetRequest(
                    userId = userId,
                    category = selectedCategory,
                    assetName = assetName,
                    amount = amount
                )
                
                RetrofitClient.apiService.addAsset(request).enqueue(object : retrofit2.Callback<Map<String, Any>> {
                    override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddAssetActivity, "Asset added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddAssetActivity, "Failed to add asset", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                        Toast.makeText(this@AddAssetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btn_save_top).setOnClickListener(saveAction)
        findViewById<View>(R.id.btn_add_asset).setOnClickListener(saveAction)
    }

    private fun setupCategories() {
        catContainers = listOf(
            findViewById(R.id.cat_bank),
            findViewById(R.id.cat_cash),
            findViewById(R.id.cat_investment),
            findViewById(R.id.cat_gold),
            findViewById(R.id.cat_property),
            findViewById(R.id.cat_vehicle),
            findViewById(R.id.cat_other)
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
        selected.setBackgroundResource(R.drawable.bg_outline_green)
        val tvSelected = selected.getChildAt(1) as? TextView
        tvSelected?.setTextColor(Color.parseColor("#00D09E"))
        tvSelected?.setTypeface(null, android.graphics.Typeface.BOLD)
        
        selectedCategory = tvSelected?.text?.toString() ?: ""
    }
}
