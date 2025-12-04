package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class SearchProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): Flow<List<Product>> =
        repository.search(query)
}