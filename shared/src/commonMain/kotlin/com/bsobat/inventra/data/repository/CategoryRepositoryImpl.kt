package com.bsobat.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import com.bsobat.inventra.data.model.Category
import inventra.CategoryQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val queries: CategoryQueries
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        queries.selectAllCategories()
            .asFlow()
            .map { query -> query.executeAsList().map { it.toDomain() } }

    override suspend fun add(category: Category) {
        queries.insertCategory(
            categoryId = category.categoryId,
            title = category.title,
            description = category.description,
            image = category.image,
            taxPercentage = category.taxPercentage
        )
    }

    override suspend fun update(category: Category) {
        queries.updateCategory(
            title = category.title,
            description = category.description,
            image = category.image,
            taxPercentage = category.taxPercentage,
            categoryId = category.categoryId
        )
    }

    override suspend fun delete(categoryId: String) {
        queries.deleteCategory(categoryId)
    }

    override suspend fun getById(categoryId: String): Category? =
        queries.selectCategoryById(categoryId).executeAsOneOrNull()?.toDomain()

    private fun inventra.Category.toDomain() = Category(
        categoryId = categoryId,
        title = title,
        description = description,
        image = image,
        taxPercentage = taxPercentage
    )
}