package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun add(category: Category)
    suspend fun update(category: Category)
    suspend fun delete(categoryId: String)
    suspend fun getById(categoryId: String): Category?
}