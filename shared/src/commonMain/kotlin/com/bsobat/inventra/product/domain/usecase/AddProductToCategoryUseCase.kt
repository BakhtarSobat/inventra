package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.repository.ProductRepository

class AddProductToCategoryUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String, categoryId: String) =
        repository.addToCategory(productId, categoryId)
}