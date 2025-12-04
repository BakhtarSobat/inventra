package com.bsobat.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bsobat.inventra.data.model.Product
import com.inventra.database.InventraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val database: InventraDatabase
) : ProductRepository {

    private val productQueries = database.productQueries
    private val categoryProductQueries = database.categoryProductQueries

    override fun observeAll(): Flow<List<Product>> {
        return productQueries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map(::mapToProduct) }
    }

    override fun observeByCategory(categoryId: String): Flow<List<Product>> {
        return categoryProductQueries.selectProductsForCategory(categoryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map(::mapToProduct) }
    }

    override suspend fun add(product: Product) {
        productQueries.insertProduct(
            productId = product.productId,
            title = product.title,
            description = product.description,
            image = product.image,
            skuCode = product.skuCode,
            barcode = product.barcode
        )
    }

    override suspend fun update(product: Product) {
        productQueries.updateProduct(
            title = product.title,
            description = product.description,
            image = product.image,
            skuCode = product.skuCode,
            barcode = product.barcode,
            productId = product.productId
        )
    }

    override suspend fun delete(productId: String) {
        productQueries.transaction {
            categoryProductQueries.deleteCategoryProductsByProduct(productId)
            productQueries.deleteProduct(productId)
        }
    }

    override suspend fun deleteAllFromCategory(categoryId: String) {
        productQueries.transaction {
            val products = categoryProductQueries.selectProductsForCategory(categoryId)
                .executeAsList()

            categoryProductQueries.deleteCategoryProductsByCategory(categoryId)

            products.forEach { product ->
                val categoryCount =
                    categoryProductQueries.countCategoriesForProduct(product.productId)
                        .executeAsOne()

                if (categoryCount == 0L) {
                    productQueries.deleteProduct(product.productId)
                }
            }
        }
    }

    override suspend fun getById(productId: String): Product? {
        return productQueries.selectProductById(productId)
            .executeAsOneOrNull()
            ?.let(::mapToProduct)
    }

    override suspend fun addToCategory(productId: String, categoryId: String) {
        categoryProductQueries.insertCategoryProduct(categoryId, productId)
    }

    override suspend fun removeFromCategory(productId: String, categoryId: String) {
        categoryProductQueries.deleteCategoryProduct(categoryId, productId)
    }

    override suspend fun search(query: String): Flow<List<Product>> {
        return productQueries.searchByTitle("%$query%")
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.map(::mapToProduct) }
    }

    override suspend fun existsByBarcode(barcode: String): Boolean {
        return productQueries.countByBarcode(barcode).executeAsOne() > 0
    }

    override suspend fun existsBySkuCode(skuCode: String): Boolean {
        return productQueries.countBySkuCode(skuCode).executeAsOne() > 0
    }

    private fun mapToProduct(dbProduct: inventra.Product): Product {
        return Product(
            productId = dbProduct.productId,
            title = dbProduct.title,
            description = dbProduct.description,
            image = dbProduct.image,
            skuCode = dbProduct.skuCode,
            barcode = dbProduct.barcode
        )
    }
}