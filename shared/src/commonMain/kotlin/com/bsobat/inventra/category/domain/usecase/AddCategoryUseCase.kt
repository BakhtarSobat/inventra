package com.bsobat.inventra.category.domain.usecase

import com.bsobat.inventra.data.model.Category
import com.bsobat.inventra.data.repository.CategoryRepository


class AddCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) = repository.add(category)
}