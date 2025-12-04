package com.bsobat.inventra.category.domain.usecase

import com.bsobat.inventra.data.repository.CategoryRepository
import com.bsobat.inventra.product.domain.usecase.DeleteProductsFromCategoryUseCase


class DeleteCategoryUseCase(
    private val repository: CategoryRepository,
    private val deleteProductsFromCategoryUseCase: DeleteProductsFromCategoryUseCase,

    ) {
    suspend operator fun invoke(categoryId: String) {
        repository.delete(categoryId)
        deleteProductsFromCategoryUseCase(categoryId)
    }
}