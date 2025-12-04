package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetAllProductsUseCase(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeAll()
}