package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.Offer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface BasketRepository {
    val items: Flow<List<BasketItem>>
    val subtotal: Flow<Double>
    val tax: Flow<Double>
    val total: Flow<Double>

    fun add(offer: Offer, quantity: Int = 1)
    fun update(offerId: String, quantity: Int)
    fun remove(offerId: String)
    fun clear()
}
