package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository
import kotlinx.coroutines.flow.Flow

class ObserveBasketSubtotalUseCase(
    private val repository: BasketRepository
) {
    operator fun invoke(): Flow<Double> = repository.subtotal
}
