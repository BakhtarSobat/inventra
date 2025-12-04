package com.bsobat.inventra.product.domain.usecase

import com.bsobat.inventra.data.repository.ProductRepository
import com.bsobat.inventra.offer.domain.usecase.DeleteOfferUseCase

class DeleteProductsFromCategoryUseCase(
    private val repository: ProductRepository,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val deleteOfferUseCase: DeleteOfferUseCase
) {
    suspend operator fun invoke(categoryId: String) {
        getProductsByCategoryUseCase.invoke(categoryId).collect { products ->
            products.forEach { product ->
                product.productId.let { pid ->
                    deleteOfferUseCase(pid)
                }
            }
        }
        repository.deleteAllFromCategory(categoryId)
    }
}