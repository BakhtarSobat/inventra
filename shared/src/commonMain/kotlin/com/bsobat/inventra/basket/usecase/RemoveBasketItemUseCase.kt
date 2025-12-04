package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository

class RemoveBasketItemUseCase(
    private val repository: BasketRepository
) {
    operator fun invoke(offerId: String) = repository.remove(offerId)
}
