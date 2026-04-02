package com.simats.pavan_digi_wallet

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.LinearLayout
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import android.view.Gravity
import android.widget.GridLayout

class PlannedBudgetActivity : AppCompatActivity() {

    private lateinit var layoutRemindersList: LinearLayout
    private lateinit var layoutRemindersSection: View
    private lateinit var cardCalendar: View
    private lateinit var tvCalendarMonth: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvPaid: TextView
    private lateinit var tvPending: TextView
    private lateinit var calendarGrid: GridLayout
    
    private var currentMonth = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_planned_budget)

        // Set status bar to dark
        window.statusBarColor = android.graphics.Color.parseColor("#0F1520")
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        layoutRemindersList = findViewById(R.id.layout_reminders_list)
        layoutRemindersSection = findViewById(R.id.layout_reminders_section)
        cardCalendar = findViewById(R.id.card_calendar)
        tvCalendarMonth = findViewById(R.id.tv_calendar_month)
        
        tvTotal = findViewById(R.id.tv_total_planned)
        tvPaid = findViewById(R.id.tv_paid_planned)
        tvPending = findViewById(R.id.tv_pending_planned)
        calendarGrid = findViewById(R.id.calendar_grid)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val tabCalendar = findViewById<TextView>(R.id.tab_calendar)
        val tabList = findViewById<TextView>(R.id.tab_list)

        tabCalendar.setOnClickListener {
            showCalendarView(tabCalendar, tabList)
        }

        tabList.setOnClickListener {
            showListView(tabCalendar, tabList)
        }

        // Initial View
        showCalendarView(tabCalendar, tabList)
        
        findViewById<View>(R.id.btn_prev_month).setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            loadData()
        }
        
        findViewById<View>(R.id.btn_next_month).setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            loadData()
        }
        
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun showCalendarView(tabCalendar: TextView, tabList: TextView) {
        tabCalendar.setBackgroundResource(R.drawable.bg_tab_pill)
        tabCalendar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_blue)
        tabCalendar.setTextColor(android.graphics.Color.WHITE)
        
        tabList.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        tabList.setTextColor(android.graphics.Color.parseColor("#6F767E"))
        
        cardCalendar.visibility = View.VISIBLE
        layoutRemindersSection.visibility = View.GONE
    }

    private fun showListView(tabCalendar: TextView, tabList: TextView) {
        tabList.setBackgroundResource(R.drawable.bg_tab_pill)
        tabList.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_blue)
        tabList.setTextColor(android.graphics.Color.WHITE)
        
        tabCalendar.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        tabCalendar.setTextColor(android.graphics.Color.parseColor("#6F767E"))
        
        cardCalendar.visibility = View.GONE
        layoutRemindersSection.visibility = View.VISIBLE
    }

    private fun loadData() {
        val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        RetrofitClient.apiService.getReminderHistory(userId).enqueue(object : retrofit2.Callback<List<Reminder>> {
            override fun onResponse(call: retrofit2.Call<List<Reminder>>, response: retrofit2.Response<List<Reminder>>) {
                if (response.isSuccessful) {
                    val reminders = response.body() ?: emptyList()
                    updateUi(reminders)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Reminder>>, t: Throwable) {
                Toast.makeText(this@PlannedBudgetActivity, "Error loading reminders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUi(reminders: List<Reminder>) {
        setupDynamicCalendar(reminders)
        
        var total = 0.0
        var paid = 0.0
        var pending = 0.0

        layoutRemindersList.removeAllViews()
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (reminder in reminders) {
            total += reminder.amount
            if (reminder.isPaid) {
                paid += reminder.amount
            } else {
                pending += reminder.amount
                
                // Show in list ONLY IF within reminder window (dueDate - reminderDaysBefore <= today)
                try {
                    val dueDate = sdf.parse(reminder.dueDate)
                    if (dueDate != null) {
                        val remindCal = Calendar.getInstance()
                        remindCal.time = dueDate
                        remindCal.add(Calendar.DAY_OF_YEAR, -reminder.reminderDaysBefore)
                        
                        val todayTruncated = Calendar.getInstance()
                        todayTruncated.set(Calendar.HOUR_OF_DAY, 0)
                        todayTruncated.set(Calendar.MINUTE, 0)
                        todayTruncated.set(Calendar.SECOND, 0)
                        todayTruncated.set(Calendar.MILLISECOND, 0)

                        if (!todayTruncated.time.before(remindCal.time)) {
                            addReminderToList(reminder, dueDate)
                        }
                    }
                } catch (e: Exception) {
                    addReminderToList(reminder, null)
                }
            }
        }

        tvTotal.text = "₹${String.format("%,.0f", total)}"
        tvPaid.text = "₹${String.format("%,.0f", paid)}"
        tvPending.text = "₹${String.format("%,.0f", pending)}"
    }

    private fun setupDynamicCalendar(reminders: List<Reminder>) {
        calendarGrid.removeAllViews()
        
        val year = currentMonth.get(Calendar.YEAR)
        val month = currentMonth.get(Calendar.MONTH)
        
        // Month Title
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvCalendarMonth.text = monthYearFormat.format(currentMonth.time)
        
        // Days in month
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sun
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Add empty slots with proper weight to maintain column alignment
        for (i in 0 until firstDayOfWeek) {
            val empty = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            empty.layoutParams = params
            calendarGrid.addView(empty)
        }
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (day in 1..daysInMonth) {
            val dayView = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            dayView.layoutParams = params
            dayView.text = day.toString()
            dayView.gravity = Gravity.CENTER
            dayView.setTextColor(android.graphics.Color.WHITE)
            dayView.textSize = 14f
            
            // Current day for this slot
            val slotCal = Calendar.getInstance()
            slotCal.set(year, month, day)
            val slotDate = slotCal.time
            
            highlightDay(dayView, day, reminders, slotDate)
            
            dayView.setOnClickListener {
                val intent = android.content.Intent(this, AddPlannedBillActivity::class.java)
                intent.putExtra("EXTRA_DAY", day.toString())
                intent.putExtra("EXTRA_MONTH", (month + 1).toString()) // 1-indexed for backend
                intent.putExtra("EXTRA_YEAR", year.toString())
                startActivity(intent)
            }
            
            calendarGrid.addView(dayView)
        }
    }

    private fun highlightDay(view: TextView, day: Int, reminders: List<Reminder>, currentDayDate: java.util.Date) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        for (reminder in reminders) {
            try {
                val dueDate = sdf.parse(reminder.dueDate) ?: continue
                
                // 1. Check if it's the DUE day
                if (sdf.format(dueDate) == sdf.format(currentDayDate)) {
                    if (reminder.isPaid) {
                        view.setBackgroundResource(R.drawable.dot_healthy)
                        view.backgroundTintList = ContextCompat.getColorStateList(this, R.color.accent_green)
                    } else {
                        view.setTextColor(android.graphics.Color.parseColor("#E67E22"))
                        view.setBackgroundResource(R.drawable.bg_tab_pill)
                        view.backgroundTintList = ContextCompat.getColorStateList(this, R.color.surface_light)
                    }
                    return
                }
                
                // 2. Check if within reminder window
                if (!reminder.isPaid) {
                    val remindStart = Calendar.getInstance()
                    remindStart.time = dueDate
                    remindStart.add(Calendar.DAY_OF_YEAR, -reminder.reminderDaysBefore)
                    
                    if (!currentDayDate.before(remindStart.time) && !currentDayDate.after(dueDate)) {
                        view.setTextColor(android.graphics.Color.parseColor("#3498DB"))
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun addReminderToList(reminder: Reminder, dueDate: java.util.Date?) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_reminder, layoutRemindersList, false)
        view.findViewById<TextView>(R.id.tv_bill_name).text = reminder.billName
        view.findViewById<TextView>(R.id.tv_amount).text = "₹${String.format("%,.0f", reminder.amount)}"
        
        val iconView = view.findViewById<android.widget.ImageView>(R.id.img_icon)
        iconView.setImageResource(getIconForBill(reminder.billName))

        if (dueDate != null) {
            val displaySdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            view.findViewById<TextView>(R.id.tv_due_date).text = "Due on ${displaySdf.format(dueDate)}"
        } else {
            view.findViewById<TextView>(R.id.tv_due_date).text = "Due on ${reminder.dueDate}"
        }
        layoutRemindersList.addView(view)
    }

    private fun getIconForBill(name: String): Int {
        val n = name.lowercase()
        return when {
            n.contains("elect") || n.contains("light") || n.contains("power") -> R.drawable.ic_electricity
            n.contains("water") -> R.drawable.ic_water
            n.contains("gas") -> R.drawable.ic_gas
            n.contains("mobile") || n.contains("recharge") || n.contains("phone") -> R.drawable.ic_mobile
            n.contains("net") || n.contains("wifi") || n.contains("broadband") -> R.drawable.ic_activity
            n.contains("rent") || n.contains("housing") -> R.drawable.ic_home
            n.contains("emi") || n.contains("loan") || n.contains("bank") -> R.drawable.ic_bank
            n.contains("sub") || n.contains("ott") || n.contains("netflix") -> R.drawable.ic_subscriptions
            n.contains("insur") || n.contains("policy") -> R.drawable.ic_shield
            n.contains("bus") || n.contains("transport") || n.contains("cab") -> R.drawable.ic_transport
            else -> R.drawable.ic_clock
        }
    }
}

