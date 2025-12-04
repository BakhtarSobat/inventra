package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.OfferRepository

class SearchOffersUseCase(
    private val repository: OfferRepository
) {
    suspend operator fun invoke(query: String): List<Offer> = repository.search(query)
}
