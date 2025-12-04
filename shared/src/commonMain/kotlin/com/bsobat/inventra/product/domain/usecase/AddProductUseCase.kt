package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.data.repository.ProductRepository

class AddProductUseCase(
    private val repository: ProductRepository,
    private val addProductToCategoryUseCase: AddProductToCategoryUseCase,
) {
    suspend operator fun invoke(product: Product, categoryId: String) {
        repository.add(product)
        addProductToCategoryUseCase(product.productId, categoryId)
    }
}