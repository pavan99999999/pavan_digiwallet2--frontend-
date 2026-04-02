package com.simats.pavan_digi_wallet

import com.google.gson.annotations.SerializedName

data class TransactionData(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category") val category: String,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("date") val date: String,
    @SerializedName("note") val note: String?,
    @SerializedName("verified") val verified: Boolean
)
