package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository

class ClearBasketUseCase(
    private val repository: BasketRepository
) {
    operator fun invoke() = repository.clear()
}
