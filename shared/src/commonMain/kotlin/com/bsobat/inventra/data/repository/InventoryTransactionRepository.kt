package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.InventoryTransaction
import kotlinx.coroutines.flow.Flow

interface InventoryTransactionRepository {
    fun observeTransactions(): Flow<List<InventoryTransaction>>
    suspend fun getTransactionsForOffer(offerId: String): List<InventoryTransaction>
    suspend fun recordTransaction(transaction: InventoryTransaction)
    suspend fun deleteTransaction(id: String)
    suspend fun deleteAll()
}