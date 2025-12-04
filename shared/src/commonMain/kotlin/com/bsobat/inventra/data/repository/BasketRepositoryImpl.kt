package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.Offer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BasketRepositoryImpl : BasketRepository {
    private val _items = MutableStateFlow<List<BasketItem>>(emptyList())
    override val items: Flow<List<BasketItem>> = _items.asStateFlow()

    override val subtotal: Flow<Double> = items.map(::calculateSubtotal)
    override val tax: Flow<Double> = items.map(::calculateTax)
    override val total: Flow<Double> = combine(subtotal, tax) { sub, tax -> sub + tax }

    override fun add(offer: Offer, quantity: Int) {
        if(quantity < 1) {
            return remove(offer.offerId)
        }
        updateItems { current ->
            val existing = current.firstOrNull { it.offer.offerId == offer.offerId }
            if (existing == null) {
                current + BasketItem(offer, quantity)
            } else {
                current.map {
                    if (it.offer.offerId == offer.offerId) it.copy(quantity = it.quantity + quantity) else it
                }
            }
        }
    }

    override fun update(offerId: String, quantity: Int) {
        updateItems { current ->
            if (quantity <= 0) current.filterNot { it.offer.offerId == offerId }
            else current.map {
                if (it.offer.offerId == offerId) it.copy(quantity = quantity) else it
            }
        }
    }

    override fun remove(offerId: String) {
        updateItems { current -> current.filterNot { it.offer.offerId == offerId } }
    }

    override fun clear() {
        _items.value = emptyList()
    }

    private fun updateItems(transform: (List<BasketItem>) -> List<BasketItem>) {
        _items.value = transform(_items.value)
    }

    private fun calculateSubtotal(items: List<BasketItem>) =
        items.sumOf { it.offer.price * it.quantity }

    private fun calculateTax(items: List<BasketItem>) =
        items.sumOf { it.offer.price * it.quantity * (it.offer.taxPercentage / 100.0) }
}
