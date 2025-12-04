package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    fun observeByCategory(categoryId: String): Flow<List<Product>>
    suspend fun add(product: Product)
    suspend fun update(product: Product)
    suspend fun delete(productId: String)
    suspend fun deleteAllFromCategory(categoryId: String)
    suspend fun getById(productId: String): Product?
    suspend fun addToCategory(productId: String, categoryId: String)
    suspend fun removeFromCategory(productId: String, categoryId: String)
    suspend fun search(query: String): Flow<List<Product>>
    suspend fun existsByBarcode(barcode: String): Boolean
    suspend fun existsBySkuCode(skuCode: String): Boolean
}