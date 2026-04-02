package com.simats.pavan_digi_wallet

import com.google.gson.annotations.SerializedName

data class Reminder(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("bill_name") val billName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("reminder_days_before") val reminderDaysBefore: Int,
    @SerializedName("is_paid") val isPaid: Boolean = false
)

data class ReminderResponse(
    @SerializedName("message") val message: String,
    @SerializedName("reminder_trigger_date") val reminderTriggerDate: String
)
