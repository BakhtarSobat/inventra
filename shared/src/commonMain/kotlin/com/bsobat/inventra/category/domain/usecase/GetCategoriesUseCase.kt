package com.bsobat.inventra.category.domain.usecase

import com.bsobat.inventra.data.repository.CategoryRepository

class GetCategoriesUseCase(
    private val repository: CategoryRepository
) {
    operator fun invoke() = repository.observeAll()
}