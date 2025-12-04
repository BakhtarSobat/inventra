package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.OfferRepository
import kotlinx.coroutines.flow.Flow

class ObserveOffersByProductUseCase(
    private val repository: OfferRepository
) {
    operator fun invoke(productId: String): Flow<List<Offer>> =
        repository.observeByProduct(productId)
}
