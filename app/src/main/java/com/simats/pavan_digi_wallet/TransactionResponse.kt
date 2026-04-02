package com.simats.pavan_digi_wallet

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("message") val message: String,
    @SerializedName("id") val id: Int
)
