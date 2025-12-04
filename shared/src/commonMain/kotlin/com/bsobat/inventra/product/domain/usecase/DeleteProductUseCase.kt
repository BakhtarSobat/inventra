package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.repository.ProductRepository
import com.bsobat.inventra.offer.domain.usecase.DeleteOfferUseCase

class DeleteProductUseCase(
    private val repository: ProductRepository,
    private val deleteOfferUseCase: DeleteOfferUseCase

) {
    suspend operator fun invoke(productId: String){
        repository.delete(productId)
        deleteOfferUseCase(productId)
    }
}