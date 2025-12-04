package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class Sale @OptIn(ExperimentalTime::class) constructor(
    val saleId: String,
    val timestamp: String,
    val eventId: String?,
    val items: List<SaleItem>,
    val payments: List<PaymentPart>
)