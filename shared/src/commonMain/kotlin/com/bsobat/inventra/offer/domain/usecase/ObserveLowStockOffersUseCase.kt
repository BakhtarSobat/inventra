package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.OfferRepository
import kotlinx.coroutines.flow.Flow

class ObserveLowStockOffersUseCase(
    private val repository: OfferRepository
) {
    operator fun invoke(threshold: Int): Flow<List<Offer>> = repository.observeLowStock(threshold)
}
