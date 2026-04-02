package com.simats.pavan_digi_wallet

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnSkip: TextView
    private lateinit var loginContainer: ViewGroup
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        // Light Theme Status Bar
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btn_next)
        btnBack = findViewById(R.id.btn_back)
        btnSkip = findViewById(R.id.btn_skip)
        loginContainer = findViewById(R.id.login_container)
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)

        val pages = listOf(
            OnboardingAdapter.OnboardingPage(
                "Track Every Rupee",
                "Know exactly where your money goes each day. Complete visibility over every transaction.",
                R.drawable.ic_money_bag,
                ContextCompat.getColor(this, R.color.surface_variant)
            ),
            OnboardingAdapter.OnboardingPage(
                "AI That Guides You",
                "Your personal AI coach gives you personalized tips every week based on your spending.",
                R.drawable.ic_robot,
                ContextCompat.getColor(this, R.color.surface_variant)
            ),
            OnboardingAdapter.OnboardingPage(
                "Reach Goals Faster",
                "Set goals, track savings, celebrate milestones. Financial freedom is within reach.",
                R.drawable.ic_trophy,
                ContextCompat.getColor(this, R.color.surface_variant)
            )
        )

        viewPager.adapter = OnboardingAdapter(pages)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUI(position)
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < pages.size - 1) {
                viewPager.currentItem += 1
            } else {
                goToLogin()
            }
        }

        btnBack.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        btnSkip.setOnClickListener { goToLogin() }
        findViewById<View>(R.id.btn_login_link).setOnClickListener { goToLogin() }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun updateUI(position: Int) {
        // Update Indicators
        val activeParams = LinearLayout.LayoutParams(dpToPx(28), dpToPx(10)).apply { marginEnd = dpToPx(8) }
        val inactiveParams = LinearLayout.LayoutParams(dpToPx(10), dpToPx(10)).apply { marginEnd = dpToPx(8) }
        val lastInactiveParams = LinearLayout.LayoutParams(dpToPx(10), dpToPx(10))

        indicator1.layoutParams = if (position == 0) activeParams else inactiveParams
        indicator1.setBackgroundResource(if (position == 0) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive)

        indicator2.layoutParams = if (position == 1) activeParams else inactiveParams
        indicator2.setBackgroundResource(if (position == 1) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive)

        indicator3.layoutParams = if (position == 2) activeParams else lastInactiveParams
        indicator3.setBackgroundResource(if (position == 2) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive)

        // Update Buttons
        btnBack.visibility = if (position > 0) View.VISIBLE else View.GONE
        
        if (position == 2) {
            btnNext.text = "Get Started"
            btnSkip.visibility = View.GONE
            loginContainer.visibility = View.VISIBLE
        } else {
            btnNext.text = "Next"
            btnSkip.visibility = View.VISIBLE
            loginContainer.visibility = View.GONE
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
