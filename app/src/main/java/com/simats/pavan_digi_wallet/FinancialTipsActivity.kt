package com.simats.pavan_digi_wallet

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast

class FinancialTipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_financial_tips)

        // Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle back button
        findViewById<View>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup expandable cards
        setupExpandableCard(R.id.card_tip_2, R.id.expandable_tip_2, R.id.arrow_tip_2)
        setupExpandableCard(R.id.card_tip_3, R.id.expandable_tip_3, R.id.arrow_tip_3)
        setupExpandableCard(R.id.card_tip_4, R.id.expandable_tip_4, R.id.arrow_tip_4)
        setupExpandableCard(R.id.card_tip_5, R.id.expandable_tip_5, R.id.arrow_tip_5)
        setupExpandableCard(R.id.card_tip_6, R.id.expandable_tip_6, R.id.arrow_tip_6)
        setupExpandableCard(R.id.card_tip_7, R.id.expandable_tip_7, R.id.arrow_tip_7)
        setupExpandableCard(R.id.card_tip_8, R.id.expandable_tip_8, R.id.arrow_tip_8)
        setupExpandableCard(R.id.card_tip_9, R.id.expandable_tip_9, R.id.arrow_tip_9)
        setupExpandableCard(R.id.card_tip_10, R.id.expandable_tip_10, R.id.arrow_tip_10)
        setupExpandableCard(R.id.card_tip_11, R.id.expandable_tip_11, R.id.arrow_tip_11)
        setupExpandableCard(R.id.card_tip_12, R.id.expandable_tip_12, R.id.arrow_tip_12)

        // Setup 'Coming Soon' Action Buttons for all tips
        val tipIndices = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        for (i in tipIndices) {
            setupComingSoonAction(i)
        }

        // Setup listeners for category chips
        findViewById<View>(R.id.tag_all)?.setOnClickListener {
            Toast.makeText(this, "Showing all tips", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.tag_budgeting)?.setOnClickListener {
            Toast.makeText(this, "Budgeting tips filter coming soon!", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.tag_emergency)?.setOnClickListener {
            Toast.makeText(this, "Emergency fund tips filter coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Setup listener for focus card
        findViewById<View>(R.id.card_focus)?.setOnClickListener {
            Toast.makeText(this, "This challenge is active! Check back for progress updates.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupExpandableCard(cardId: Int, expandableId: Int, arrowId: Int) {
        val card = findViewById<View>(cardId) ?: return
        val expandable = findViewById<View>(expandableId) ?: return
        val arrow = findViewById<View>(arrowId) ?: return

        card.setOnClickListener {
            if (expandable.visibility == View.GONE) {
                expandable.visibility = View.VISIBLE
                arrow.animate().rotation(180f).setDuration(300).start()
            } else {
                expandable.visibility = View.GONE
                arrow.animate().rotation(0f).setDuration(300).start()
            }
        }
    }

    private fun setupComingSoonAction(tipIndex: Int) {
        val videoBtnId = resources.getIdentifier("btn_video_$tipIndex", "id", packageName)
        val readBtnId = resources.getIdentifier("btn_read_$tipIndex", "id", packageName)
        val remindBtnId = resources.getIdentifier("btn_remind_$tipIndex", "id", packageName)

        if (videoBtnId != 0) {
            findViewById<View>(videoBtnId)?.setOnClickListener {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(getYoutubeUrl(tipIndex)))
                startActivity(intent)
            }
        }
        if (readBtnId != 0) {
            findViewById<View>(readBtnId)?.setOnClickListener {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(getArticleUrl(tipIndex)))
                startActivity(intent)
            }
        }
        if (remindBtnId != 0) {
            findViewById<View>(remindBtnId)?.setOnClickListener {
                Toast.makeText(this, "Reminder feature coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getYoutubeUrl(index: Int): String {
        return when (index) {
            2 -> "https://www.youtube.com/watch?v=5_Y_P8E1S5U" // Emergency Fund
            3 -> "https://www.youtube.com/watch?v=sVKQn2I4HDM" // Pay Yourself First
            4 -> "https://www.youtube.com/watch?v=F0O5vA06iLo" // Investing Early
            5 -> "https://www.youtube.com/watch?v=F07_v_D36eI" // Debt
            9 -> "https://www.youtube.com/watch?v=sVKQn2I4HDM" // 50/30/20
            10 -> "https://www.youtube.com/watch?v=Lp7E973zozc" // Zero Based
            else -> "https://www.youtube.com/watch?v=sVKQn2I4HDM"
        }
    }

    private fun getArticleUrl(index: Int): String {
        return when (index) {
            2 -> "https://www.nerdwallet.com/article/banking/emergency-fund-why-it-matters" // Emergency Fund
            3 -> "https://www.investopedia.com/terms/p/payyourselffirst.asp" // Pay Yourself First
            4 -> "https://www.nerdwallet.com/article/investing/how-to-start-investing" // Investing Early
            5 -> "https://www.investopedia.com/terms/d/debt-snowball-method.asp" // Debt
            9 -> "https://www.nerdwallet.com/article/finance/nerdwallet-budget-calculator" // 50/30/20
            10 -> "https://www.nerdwallet.com/blog/finance/zero-based-budgeting-explained/" // Zero Based
            else -> "https://www.investopedia.com/financial-term-dictionary-4769738"
        }
    }
}
