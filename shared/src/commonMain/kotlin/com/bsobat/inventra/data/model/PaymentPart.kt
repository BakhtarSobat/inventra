package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentPart(
    val method: String, // e.g. "cash", "qr", "card"
    val amount: Double,
    val qrCodeData: String? = null,
    val status: String = "pending", // e.g. "pending", "completed", "failed"
    val note: String? = null
)