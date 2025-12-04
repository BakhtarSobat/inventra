package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class InventoryTransaction @OptIn(ExperimentalTime::class) constructor(
    val transactionId: String,
    val offerId: String,
    val changeAmount: Int,
    val reason: String,
    val timestamp: String,
    val eventId: String?
)