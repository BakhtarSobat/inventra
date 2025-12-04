package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun observeSales(): Flow<List<Sale>>
    suspend fun getSaleById(id: String): Sale?
    suspend fun upsertSale(sale: Sale)
    suspend fun deleteSale(id: String)
    suspend fun deleteAll()
}