package com.simats.pavan_digi_wallet

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/add-transaction")
    fun addTransaction(
        @Body transaction: TransactionRequest
    ): Call<TransactionResponse>

    @GET("/summary/{user_id}")
    fun getSummary(
        @Path("user_id") userId: Int
    ): Call<Map<String, Map<String, Double>>>

    @GET("/income-summary/{user_id}")
    fun getIncomeSummary(
        @Path("user_id") userId: Int
    ): Call<Map<String, Double>>

    @GET("/expense-category-summary/{user_id}")
    fun getExpenseCategorySummary(
        @Path("user_id") userId: Int
    ): Call<Map<String, Double>>

    @GET("/expense-summary/{user_id}")
    fun getExpenseSummary(
        @Path("user_id") userId: Int
    ): Call<Map<String, Double>>

    @GET("/income-history/{user_id}")
    fun getIncomeHistory(
        @Path("user_id") userId: Int
    ): Call<List<TransactionData>>

    @GET("/expense-history/{user_id}")
    fun getExpenseHistory(@Path("user_id") userId: Int): Call<List<TransactionData>>

    @GET("/get-history/{user_id}")
    fun getHistory(@Path("user_id") userId: Int): Call<List<TransactionData>>

    @DELETE("delete-transaction/{transaction_id}")
    fun deleteTransaction(@Path("transaction_id") transactionId: Int): Call<Map<String, Any>>

    @DELETE("delete-ai-transactions/{user_id}")
    fun deleteAiTransactions(@Path("user_id") userId: Int): Call<Map<String, Any>>

    @GET("/unverified-transactions/{user_id}")
    fun getUnverified(@Path("user_id") userId: Int): Call<List<TransactionData>>

    @POST("/verify-transaction/{transaction_id}")
    fun verifyTransaction(@Path("transaction_id") transactionId: Int): Call<Map<String, Any>>

    @POST("/ai-chat")
    fun aiChat(@Body request: ChatRequest): Call<ChatResponse>

    @POST("/add-reminder")
    fun addReminder(
        @Body reminder: ReminderRequest
    ): Call<ReminderResponse>

    @GET("/reminder-history/{user_id}")
    fun getReminderHistory(
        @Path("user_id") userId: Int
    ): Call<List<Reminder>>

    @GET("/upcoming-reminders/{user_id}")
    fun getUpcomingReminders(
        @Path("user_id") userId: Int
    ): Call<List<Reminder>>

    @Multipart
    @POST("/upload-statement")
    fun uploadStatement(
        @Query("user_id") userId: Int,
        @Part file: MultipartBody.Part
    ): Call<StatementUploadResponse>

    @POST("/add-goal")
    fun createGoal(
        @Body request: GoalRequest
    ): Call<Map<String, Any>>

    @GET("/goals/{user_id}")
    fun getGoals(
        @Path("user_id") userId: Int
    ): Call<List<GoalData>>

    @POST("/signup")
    fun signup(@Body request: SignupRequest): Call<AuthResponse>

    @POST("/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("/google-login")
    fun googleLogin(@Body request: GoogleLoginRequest): Call<AuthResponse>


    @POST("/predict-balance")
    fun predictBalance(
        @Body request: ForecastRequest
    ): Call<PredictBalanceResponse>

    @GET("/ai-insights/{user_id}")
    fun getAiInsights(
        @Path("user_id") userId: Int
    ): Call<AiInsightResponse>

    @POST("/add-subscription")
    fun addSubscription(
        @Body subscription: SubscriptionRequest
    ): Call<Map<String, Any>>
    @POST("/add-asset")
    fun addAsset(
        @Body request: AssetRequest
    ): Call<Map<String, Any>>

    @GET("/assets/{user_id}")
    fun getAssets(
        @Path("user_id") userId: Int
    ): Call<List<AssetData>>

    @POST("/add-liability")
    fun addLiability(
        @Body request: LiabilityRequest
    ): Call<Map<String, Any>>

    @GET("/liabilities/{user_id}")
    fun getLiabilities(
        @Path("user_id") userId: Int
    ): Call<List<LiabilityData>>

    @GET("/subscriptions/{user_id}")
    fun getSubscriptions(
        @Path("user_id") userId: Int
    ): Call<List<SubscriptionData>>

    @GET("/can-afford/{user_id}")
    fun canAfford(
        @Path("user_id") userId: Int,
        @Query("amount") amount: Double
    ): Call<CanAffordResponse>

    @POST("/add-training-data")
    fun addTrainingData(@Body request: NewBalanceData): Call<Map<String, Any>>

    @GET("/model-health")
    fun getModelHealth(): Call<Map<String, Any>>

    @POST("/get-insights")
    fun getDetailedInsights(@Body request: InsightRequest): Call<DetailedInsightResponse>

    @POST("/get-budget-recommendations")
    fun getBudgetRecommendations(@Body request: BudgetRequest): Call<BudgetRecommendationResponse>

    @POST("/analyze")
    fun analyzeFinance(@Body request: FinanceAnalysisRequest): Call<FinanceAnalysisResponse>

    @GET("/insights/{user_id}")
    fun getInsights(@Path("user_id") userId: Int): Call<InsightsResponse>

    @POST("/chat-new")
    fun chatNew(@Body request: ChatRequest): Call<ChatResponse>

    @GET("/download-report-rl-final/{user_id}")
    fun downloadReport(@Path("user_id") userId: Int): Call<okhttp3.ResponseBody>

    @POST("/update-fcm-token/{user_id}")
    fun updateFcmToken(
        @Path("user_id") userId: Int,
        @Body request: FcmTokenRequest
    ): Call<Map<String, Any>>

    @POST("/gemini-chat")
    fun geminiChatPro(@Body request: ChatRequest): Call<ChatResponse>

    @GET("/download-enhanced-report/{user_id}")
    fun downloadEnhancedReport(@Path("user_id") userId: Int): Call<okhttp3.ResponseBody>
}

data class FcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

data class InsightsResponse(
    @SerializedName("analysis") val analysis: InsightsAnalysis,
    @SerializedName("ai_advice") val aiAdvice: String
)

data class InsightsAnalysis(
    @SerializedName("summary") val summary: FinancialSummary,
    @SerializedName("subscriptions") val subscriptions: List<String>,
    @SerializedName("planned_payments") val plannedPayments: List<Int>,
    @SerializedName("month_comparison") val monthComparison: String?,
    @SerializedName("smart_changes") val smartChanges: String?,
    @SerializedName("predicted_balance") val predictedBalance: String?,
    @SerializedName("goal_prediction") val goalPrediction: String?,
    @SerializedName("smart_tips") val smartTips: String?,
    @SerializedName("financial_health") val financialHealth: String?,
    @SerializedName("financial_score") val financialScore: String?,
    @SerializedName("behavior") val behavior: String?,
    @SerializedName("budget_plan") val budgetPlan: String?,
    @SerializedName("risk_alerts") val riskAlerts: String?,
    @SerializedName("personality") val personality: String?,
    @SerializedName("anomalies") val anomalies: String?
)

data class FinancialSummary(
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double,
    @SerializedName("savings") val savings: Double
)

data class FinanceAnalysisRequest(
    @SerializedName("income") val income: Double,
    @SerializedName("food") val food: Double,
    @SerializedName("shopping") val shopping: Double,
    @SerializedName("transport") val transport: Double,
    @SerializedName("question") val question: String
)

data class FinanceAnalysisResponse(
    @SerializedName("analysis") val analysis: Map<String, Any>,
    @SerializedName("ai_advice") val aiAdvice: String,
    @SerializedName("pdf_report_url") val pdfReportUrl: String
)

data class BudgetRequest(
    @SerializedName("monthly_income") val monthlyIncome: Double,
    @SerializedName("dining_spend") val diningSpend: Double,
    @SerializedName("shopping_spend") val shoppingSpend: Double,
    @SerializedName("transport_spend") val transportSpend: Double,
    @SerializedName("groceries_spend") val groceriesSpend: Double,
    @SerializedName("entertainment_spend") val entertainmentSpend: Double,
    @SerializedName("last_month_dining") val lastMonthDining: Double,
    @SerializedName("last_month_shopping") val lastMonthShopping: Double,
    @SerializedName("last_month_transport") val lastMonthTransport: Double,
    @SerializedName("last_month_groceries") val lastMonthGroceries: Double,
    @SerializedName("last_month_entertainment") val lastMonthEntertainment: Double,
    @SerializedName("dining_change_percent") val diningChangePercent: Double,
    @SerializedName("shopping_change_percent") val shoppingChangePercent: Double,
    @SerializedName("transport_change_percent") val transportChangePercent: Double,
    @SerializedName("groceries_change_percent") val groceriesChangePercent: Double,
    @SerializedName("entertainment_change_percent") val entertainmentChangePercent: Double,
    @SerializedName("total_spending") val totalSpending: Double,
    @SerializedName("savings_change") val savingsChange: Double
)

data class BudgetRecommendationResponse(
    @SerializedName("current_spending") val currentSpending: Map<String, String>,
    @SerializedName("recommended_budget") val recommendedBudget: Map<String, String>,
    @SerializedName("potential_savings") val potentialSavings: String,
    @SerializedName("tips") val tips: List<String>
)

data class InsightRequest(
    @SerializedName("monthly_income") val monthlyIncome: Double,
    @SerializedName("dining_spend") val diningSpend: Double,
    @SerializedName("last_month_dining") val lastMonthDining: Double,
    @SerializedName("shopping_spend") val shoppingSpend: Double,
    @SerializedName("last_month_shopping") val lastMonthShopping: Double,
    @SerializedName("transport_spend") val transportSpend: Double,
    @SerializedName("last_month_transport") val lastMonthTransport: Double,
    @SerializedName("groceries_spend") val groceriesSpend: Double,
    @SerializedName("last_month_groceries") val lastMonthGroceries: Double,
    @SerializedName("entertainment_spend") val entertainmentSpend: Double,
    @SerializedName("last_month_entertainment") val lastMonthEntertainment: Double,
    @SerializedName("total_spending") val totalSpending: Double,
    @SerializedName("savings_change") val savingsChange: Double
)

data class DetailedInsightResponse(
    @SerializedName("category") val category: String,
    @SerializedName("insights") val insights: List<String>,
    @SerializedName("spending_ratio") val spendingRatio: String,
    @SerializedName("advice") val advice: String?,
    @SerializedName("dashboard_card") val dashboardCard: DashboardCard
)

data class DashboardCard(
    @SerializedName("title") val title: String,
    @SerializedName("points") val points: List<String>,
    @SerializedName("status") val status: String
)

data class NewBalanceData(
    @SerializedName("monthly_income") val monthlyIncome: Double,
    @SerializedName("current_balance") val currentBalance: Double,
    @SerializedName("food_expense_monthly") val foodExpenseMonthly: Double,
    @SerializedName("transport_expense_monthly") val transportExpenseMonthly: Double,
    @SerializedName("shopping_expense_monthly") val shoppingExpenseMonthly: Double,
    @SerializedName("utilities_expense_monthly") val utilitiesExpenseMonthly: Double,
    @SerializedName("rent_bill_upcoming") val rentBillUpcoming: Double,
    @SerializedName("internet_bill_upcoming") val internetBillUpcoming: Double,
    @SerializedName("electricity_bill_upcoming") val electricityBillUpcoming: Double,
    @SerializedName("subscription_netflix") val subscriptionNetflix: Double,
    @SerializedName("subscription_spotify") val subscriptionSpotify: Double,
    @SerializedName("subscription_prime") val subscriptionPrime: Double,
    @SerializedName("other_upcoming_bills") val otherUpcomingBills: Double,
    @SerializedName("predicted_balance_15_days") val predictedBalance15Days: Double
)

data class ForecastRequest(
    @SerializedName("monthly_income") val monthlyIncome: Double,
    @SerializedName("current_balance") val currentBalance: Double,
    @SerializedName("food_expense_monthly") val foodExpenseMonthly: Double,
    @SerializedName("transport_expense_monthly") val transportExpenseMonthly: Double,
    @SerializedName("shopping_expense_monthly") val shoppingExpenseMonthly: Double,
    @SerializedName("utilities_expense_monthly") val utilitiesExpenseMonthly: Double,
    @SerializedName("rent_bill_upcoming") val rentBillUpcoming: Double,
    @SerializedName("internet_bill_upcoming") val internetBillUpcoming: Double,
    @SerializedName("electricity_bill_upcoming") val electricityBillUpcoming: Double,
    @SerializedName("subscription_netflix") val subscriptionNetflix: Double,
    @SerializedName("subscription_spotify") val subscriptionSpotify: Double,
    @SerializedName("subscription_prime") val subscriptionPrime: Double,
    @SerializedName("other_upcoming_bills") val otherUpcomingBills: Double,
    @SerializedName("purchase_amount") val purchaseAmount: Double = 0.0
)


data class SubscriptionData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("billing_cycle") val billingCycle: String,
    @SerializedName("next_billing_date") val nextBillingDate: String,
    @SerializedName("reminder_enabled") val reminderEnabled: Boolean,
    @SerializedName("notes") val notes: String?
)

data class CanAffordResponse(
    @SerializedName("income_this_month") val income: Double,
    @SerializedName("expense_this_month") val expense: Double,
    @SerializedName("unpaid_bills") val unpaidBills: Double,
    @SerializedName("available_balance") val availableBalance: Double,
    @SerializedName("purchase_amount") val purchaseAmount: Double,
    @SerializedName("can_afford") val canAfford: Boolean,
    @SerializedName("suggestion") val suggestion: String
)

data class LiabilityData(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("liability_name") val liabilityName: String,
    @SerializedName("amount") val amount: Double
)

data class LiabilityRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("liability_name") val liabilityName: String,
    @SerializedName("amount") val amount: Double
)

data class AssetData(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("asset_name") val assetName: String,
    @SerializedName("amount") val amount: Double
)

data class AssetRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("asset_name") val assetName: String,
    @SerializedName("amount") val amount: Double
)
data class SubscriptionRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("billing_cycle") val billing_cycle: String,
    @SerializedName("next_billing_date") val next_billing_date: String,
    @SerializedName("reminder_enabled") val reminder_enabled: Boolean = true,
    @SerializedName("notes") val notes: String = ""
)

data class PredictBalanceResponse(
    @SerializedName("current_balance") val currentBalance: Double,
    @SerializedName("predicted_balance") val predictedBalance: Double,
    @SerializedName("forecast_days") val forecastDays: Int,
    @SerializedName("advice") val advice: String?
)

data class AiInsightResponse(
    @SerializedName("current_month_expense") val currentMonthExpense: Double,
    @SerializedName("last_month_expense") val lastMonthExpense: Double,
    @SerializedName("change_percentage") val changePercentage: Double,
    @SerializedName("insight") val insight: String
)

data class TransactionRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category") val category: String,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("date") val date: String,
    @SerializedName("note") val note: String = "",
    @SerializedName("verified") val verified: Boolean = false
)

data class ReminderRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("bill_name") val billName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("reminder_days_before") val reminderDaysBefore: Int
)

data class GoalRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("goal_name") val goalName: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("saved_amount") val savedAmount: Double,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("monthly_payment") val monthlyPayment: Double
)

data class GoalData(
    @SerializedName("id") val id: Int,
    @SerializedName("goal_name") val goalName: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("saved_amount") val savedAmount: Double,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("monthly_payment") val monthlyPayment: Double,
    @SerializedName("progress_percentage") val progressPercentage: Double,
    @SerializedName("months_remaining") val monthsRemaining: Int,
    @SerializedName("days_remaining") val daysRemaining: Int,
    @SerializedName("status") val status: String
)

data class GoalResponse(
    @SerializedName("status") val status: String,
    @SerializedName("id") val id: Int
)

data class SignupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class GoogleLoginRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)
data class StatementUploadResponse(
    @SerializedName("message") val message: String,
    @SerializedName("transactions_added") val transactionsAdded: Int
)

data class ChatRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("message") val message: String
)

data class ChatResponse(
    @SerializedName("reply") val reply: String,
    @SerializedName("explanation") val explanation: String? = null
)
