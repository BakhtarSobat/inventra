package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleItem(
    val offerId: String,
    val description: String?,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val taxPercentage: Double = 0.0,
    val inventoryAdjusted: Boolean = false
)