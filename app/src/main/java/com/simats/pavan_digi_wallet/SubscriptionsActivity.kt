package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscriptionsActivity : AppCompatActivity() {
    
    private lateinit var layoutList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_subscriptions)

        // Dark theme status bar
        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        layoutList = findViewById(R.id.layout_subscriptions_items)
        
        findViewById<View>(R.id.btn_settings).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_add_sub).setOnClickListener {
            startActivity(Intent(this, AddSubscriptionActivity::class.java))
        }

        fetchSubscriptions()
    }

    override fun onResume() {
        super.onResume()
        fetchSubscriptions()
    }

    private fun fetchSubscriptions() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        RetrofitClient.apiService.getSubscriptions(userId).enqueue(object : Callback<List<SubscriptionData>> {
            override fun onResponse(call: Call<List<SubscriptionData>>, response: Response<List<SubscriptionData>>) {
                if (response.isSuccessful && response.body() != null) {
                    populateList(response.body()!!)
                }
            }

            override fun onFailure(call: Call<List<SubscriptionData>>, t: Throwable) {
                Toast.makeText(this@SubscriptionsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateList(subs: List<SubscriptionData>) {
        layoutList.removeAllViews()
        subs.forEach { sub ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_category, layoutList, false)
            
            // Style for dark theme
            val card = view as? androidx.cardview.widget.CardView
            card?.setCardBackgroundColor(Color.parseColor("#171E2B"))

            view.findViewById<TextView>(R.id.tv_cat_name).apply {
                text = sub.name
                setTextColor(Color.WHITE)
            }
            view.findViewById<TextView>(R.id.tv_cat_amount).apply {
                text = "₹${String.format("%,.0f", sub.amount)}"
                setTextColor(Color.parseColor("#00D09E"))
            }
            view.findViewById<TextView>(R.id.tv_cat_percent).apply {
                text = "${sub.billingCycle} • Next: ${sub.nextBillingDate}"
                setTextColor(Color.parseColor("#9A9FA5"))
            }
            
            layoutList.addView(view)
        }
    }
}
