package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.BasketRepository

class AddBasketItemUseCase(
    private val repository: BasketRepository
) {
    operator fun invoke(offer: Offer, quantity: Int = 1) = repository.add(offer, quantity)
}
