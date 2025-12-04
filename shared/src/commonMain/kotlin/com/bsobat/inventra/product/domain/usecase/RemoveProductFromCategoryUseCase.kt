package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.repository.ProductRepository

class RemoveProductFromCategoryUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String, categoryId: String) =
        repository.removeFromCategory(productId, categoryId)
}