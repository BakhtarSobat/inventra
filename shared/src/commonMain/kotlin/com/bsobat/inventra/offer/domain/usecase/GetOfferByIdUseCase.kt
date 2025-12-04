package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.OfferRepository

class GetOfferByIdUseCase(
    private val repository: OfferRepository
) {
    suspend operator fun invoke(offerId: String): Offer? = repository.getById(offerId)
}
