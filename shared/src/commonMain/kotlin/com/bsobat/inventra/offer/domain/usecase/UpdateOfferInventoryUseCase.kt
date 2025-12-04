package com.bsobat.inventra.offer.domain.usecase

import com.bsobat.inventra.data.repository.OfferRepository

class UpdateOfferInventoryUseCase(
    private val repository: OfferRepository
) {
    suspend operator fun invoke(offerId: String, newAmount: Int) =
        repository.updateInventory(offerId, newAmount)
}
