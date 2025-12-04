package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Offer(
    val offerId: String,
    val productId: String,
    val title: String,
    var image: String? = null,
    val amountInInventory: Int = Int.MAX_VALUE,
    val type: String = "standard", // e.g. "standard", "bundle"
    val price: Double,
    val uom: String, // e.g. "pcs", "kg"
    val taxPercentage: Double = 0.0
)