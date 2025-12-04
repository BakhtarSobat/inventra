package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetProductsByCategoryUseCase(
    private val repository: ProductRepository
) {
    operator fun invoke(categoryId: String): Flow<List<Product>> =
        repository.observeByCategory(categoryId)
}