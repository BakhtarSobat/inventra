package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.OfferRepository

class AddOfferUseCase(
    private val repository: OfferRepository
) {
    suspend operator fun invoke(offer: Offer) = repository.add(offer)
}
