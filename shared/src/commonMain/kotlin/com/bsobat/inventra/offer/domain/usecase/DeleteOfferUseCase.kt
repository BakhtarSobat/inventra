package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.repository.OfferRepository

class DeleteOfferUseCase(
    private val repository: OfferRepository
) {
    suspend operator fun invoke(offerId: String) = repository.delete(offerId)
}
