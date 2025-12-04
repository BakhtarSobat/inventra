package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.repository.BasketRepository
import kotlinx.coroutines.flow.Flow

class ObserveBasketItemsUseCase(
    private val repository: BasketRepository
) {
    operator fun invoke(): Flow<List<BasketItem>> = repository.items
}
