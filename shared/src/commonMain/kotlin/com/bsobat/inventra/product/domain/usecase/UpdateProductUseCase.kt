package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.data.repository.ProductRepository

class UpdateProductUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product) = repository.update(product)
}