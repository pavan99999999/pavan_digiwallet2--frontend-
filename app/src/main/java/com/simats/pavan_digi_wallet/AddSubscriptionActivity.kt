package com.simats.pavan_digi_wallet

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Calendar

class AddSubscriptionActivity : AppCompatActivity() {

    private lateinit var tvBillingCycle: TextView
    private lateinit var tvBillingDate: TextView
    private lateinit var etName: android.widget.EditText
    private lateinit var etAmount: android.widget.EditText
    private lateinit var llBottomButton: View
    private lateinit var layoutExisting: android.widget.LinearLayout
    private var selectedCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_subscription)

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        tvBillingCycle = findViewById(R.id.tv_billing_cycle)
        tvBillingDate = findViewById(R.id.tv_billing_date)
        etName = findViewById(R.id.et_subscription_name)
        etAmount = findViewById(R.id.et_amount)
        llBottomButton = findViewById(R.id.ll_bottom_button)
        layoutExisting = findViewById(R.id.layout_existing_subscriptions)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_save).setOnClickListener {
            saveSubscription()
        }

        findViewById<View>(R.id.btn_add_subscription_bottom).setOnClickListener {
            saveSubscription()
        }

        // Billing Cycle Selection
        findViewById<View>(R.id.ll_billing_cycle).setOnClickListener { view ->
            showBillingCyclePopup(view)
        }

        // Next Billing Date Selection
        findViewById<View>(R.id.ll_billing_date).setOnClickListener {
            showDatePicker()
        }

        setupPopularServices()
        setupCategoryClickListeners()
        setupValidationWatchers()
        refreshList()
    }

    private fun saveSubscription() {
        val name = etName.text.toString().trim()
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val category = selectedCategory
        val billingCycle = tvBillingCycle.text.toString()
        val rawDate = tvBillingDate.text.toString() // dd-mm-yyyy
        
        // Convert dd-mm-yyyy to yyyy-mm-dd for backend
        val formattedDate = try {
            val parts = rawDate.split("-")
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } catch (e: Exception) {
            "2026-03-03"
        }

        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        val request = SubscriptionRequest(
            userId = userId,
            name = name,
            category = category,
            amount = amount,
            billing_cycle = billingCycle,
            next_billing_date = formattedDate,
            reminder_enabled = true,
            notes = ""
        )

        RetrofitClient.apiService.addSubscription(request).enqueue(object : retrofit2.Callback<Map<String, Any>> {
            override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(this@AddSubscriptionActivity, "Subscription added!", android.widget.Toast.LENGTH_SHORT).show()
                    refreshList()
                    resetForm()
                } else {
                    android.widget.Toast.makeText(this@AddSubscriptionActivity, "Failed!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                android.widget.Toast.makeText(this@AddSubscriptionActivity, "Error!", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetForm() {
        etName.setText("")
        etAmount.setText("")
        tvBillingDate.text = "dd-mm-yyyy"
        selectedCategory = ""
        setupCategoryClickListeners() // reset UI
        validateFields()
    }

    private fun refreshList() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)
        RetrofitClient.apiService.getSubscriptions(userId).enqueue(object : retrofit2.Callback<List<SubscriptionData>> {
            override fun onResponse(call: retrofit2.Call<List<SubscriptionData>>, response: retrofit2.Response<List<SubscriptionData>>) {
                if (response.isSuccessful && response.body() != null) {
                    populateList(response.body()!!)
                }
            }
            override fun onFailure(call: retrofit2.Call<List<SubscriptionData>>, t: Throwable) {}
        })
    }

    private fun populateList(subs: List<SubscriptionData>) {
        layoutExisting.removeAllViews()
        subs.forEach { sub ->
            val view = android.view.LayoutInflater.from(this).inflate(R.layout.item_category, layoutExisting, false)
            val card = view as? androidx.cardview.widget.CardView
            card?.setCardBackgroundColor(android.graphics.Color.parseColor("#171E2B"))
            
            view.findViewById<TextView>(R.id.tv_cat_name).apply {
                text = sub.name
                setTextColor(android.graphics.Color.WHITE)
            }
            view.findViewById<TextView>(R.id.tv_cat_amount).apply {
                text = "₹${String.format("%,.0f", sub.amount)}"
                setTextColor(android.graphics.Color.parseColor("#00D09E"))
            }
            view.findViewById<TextView>(R.id.tv_cat_percent).apply {
                text = "${sub.billingCycle} • Next: ${sub.nextBillingDate}"
                setTextColor(android.graphics.Color.parseColor("#9A9FA5"))
            }
            layoutExisting.addView(view)
        }
    }

    private fun setupValidationWatchers() {
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateFields()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        etName.addTextChangedListener(watcher)
        etAmount.addTextChangedListener(watcher)
    }

    private fun validateFields() {
        val name = etName.text.toString().trim()
        val amount = etAmount.text.toString().trim()
        val date = tvBillingDate.text.toString()

        val isNameValid = name.isNotEmpty()
        val isAmountValid = amount.isNotEmpty()
        val isDateValid = date != "dd-mm-yyyy"
        val isCategoryValid = selectedCategory.isNotEmpty()

        if (isNameValid && isAmountValid && isDateValid && isCategoryValid) {
            llBottomButton.visibility = View.VISIBLE
        } else {
            llBottomButton.visibility = View.GONE
        }
    }

    private fun setupPopularServices() {
        val services = mapOf(
            R.id.svc_netflix to Pair("Netflix", "Entertainment"),
            R.id.svc_spotify to Pair("Spotify", "Entertainment"),
            R.id.svc_amazon to Pair("Amazon Prime", "Entertainment"),
            R.id.svc_disney to Pair("Disney+", "Entertainment"),
            R.id.svc_youtube to Pair("YouTube Premium", "Entertainment"),
            R.id.svc_apple_music to Pair("Apple Music", "Entertainment")
        )

        services.forEach { (id, data) ->
            findViewById<View>(id).setOnClickListener {
                etName.setText(data.first)
                selectCategory(data.second)
                validateFields()
            }
        }
    }

    private fun setupCategoryClickListeners() {
        val categories = mapOf(
            R.id.cat_entertainment to "Entertainment",
            R.id.cat_work to "Work",
            R.id.cat_shopping to "Shopping",
            R.id.cat_fitness to "Fitness",
            R.id.cat_education to "Education",
            R.id.cat_utilities to "Utilities",
            R.id.cat_other to "Other"
        )

        categories.forEach { (id, name) ->
            findViewById<View>(id).setOnClickListener {
                selectCategory(name)
                validateFields()
            }
        }
    }

    private fun selectCategory(categoryName: String) {
        selectedCategory = categoryName
        val categories = mapOf(
            "Entertainment" to R.id.cat_entertainment,
            "Work" to R.id.cat_work,
            "Shopping" to R.id.cat_shopping,
            "Fitness" to R.id.cat_fitness,
            "Education" to R.id.cat_education,
            "Utilities" to R.id.cat_utilities,
            "Other" to R.id.cat_other
        )

        categories.forEach { (name, id) ->
            val layout = findViewById<View>(id)
            if (name == categoryName) {
                layout.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF2B7A"))
            } else {
                layout.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#171E2B"))
            }
        }
    }

    private fun showBillingCyclePopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Monthly")
        popup.menu.add("Weekly")
        popup.menu.add("Quarterly")
        popup.menu.add("Yearly")

        popup.setOnMenuItemClickListener { item ->
            tvBillingCycle.text = item.title
            true
        }
        popup.show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val dateString = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                tvBillingDate.text = dateString
                validateFields()
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}
