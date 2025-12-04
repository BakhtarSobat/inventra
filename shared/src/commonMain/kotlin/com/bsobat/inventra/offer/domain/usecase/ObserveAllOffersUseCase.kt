package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.OfferRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllOffersUseCase(
    private val repository: OfferRepository
) {
    operator fun invoke(): Flow<List<Offer>> = repository.observeAll()
}
