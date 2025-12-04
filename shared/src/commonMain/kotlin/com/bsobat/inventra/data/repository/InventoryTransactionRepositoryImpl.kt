package com.bsobat.inventra.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bsobat.inventra.data.model.InventoryTransaction
import com.inventra.database.InventraDatabase
import inventra.Inventory_transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class InventoryTransactionRepositoryImpl(
    private val database: InventraDatabase
) : InventoryTransactionRepository {

    private val queries = database.inventoryTransactionQueries

    override fun observeTransactions(): Flow<List<InventoryTransaction>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toDomain() }
            }

    override suspend fun getTransactionsForOffer(offerId: String): List<InventoryTransaction> =
        queries.selectTransactionsForOffer(offerId)
            .executeAsList()
            .map { it.toDomain() }

    override suspend fun recordTransaction(transaction: InventoryTransaction) {
        queries.insertTransaction(
            transactionId = transaction.transactionId,
            offerId = transaction.offerId,
            changeAmount = transaction.changeAmount.toLong(),
            reason = transaction.reason,
            timestamp = transaction.timestamp,
            eventId = transaction.eventId
        )
    }

    override suspend fun deleteTransaction(id: String) {
        queries.deleteTransaction(id)
    }

    override suspend fun deleteAll() {
        queries.deleteAll()
    }

    private fun Inventory_transaction.toDomain() =
        InventoryTransaction(
            transactionId = transactionId,
            offerId = offerId,
            changeAmount = changeAmount.toInt(),
            reason = reason,
            timestamp = timestamp,
            eventId = eventId
        )
}
