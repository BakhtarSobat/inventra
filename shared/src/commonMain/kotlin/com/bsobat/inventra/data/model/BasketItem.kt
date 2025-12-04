package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BasketItem(
    val offer: Offer,
    val quantity: Int
)
