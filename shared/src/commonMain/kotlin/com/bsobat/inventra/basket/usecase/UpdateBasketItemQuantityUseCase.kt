package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository

class UpdateBasketItemQuantityUseCase(
    private val repository: BasketRepository
) {
    operator fun invoke(offerId: String, quantity: Int) = repository.update(offerId, quantity)
}
