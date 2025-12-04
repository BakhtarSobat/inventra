package com.bsobat.inventra.data.repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.data.model.SaleItem
import com.inventra.database.InventraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class SaleRepositoryImpl(
    private val database: InventraDatabase
) : SaleRepository {

    private val saleQueries = database.saleQueries

    override fun observeSales(): Flow<List<Sale>> =
        saleQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { getSaleById(it.saleId)!! }
            }

    override suspend fun getSaleById(id: String): Sale? {
        val sale = saleQueries.selectSaleById(id).executeAsOneOrNull() ?: return null
        
        val items = saleQueries.selectSaleItems(id).executeAsList().map { item ->
            SaleItem(
                offerId = item.offerId,
                quantity = item.quantity.toInt(),
                description = item.description,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice,
                taxPercentage = item.taxPercentage,
                inventoryAdjusted = item.inventoryAdjusted != 0L
            )
        }
        
        val payments = saleQueries.selectPaymentParts(id).executeAsList().map { payment ->
            PaymentPart(
                method = payment.method,
                amount = payment.amount,
                qrCodeData = payment.qrCodeData,
                status = payment.status,
                note = payment.note
            )
        }
        
        return Sale(
            saleId = sale.saleId,
            timestamp = sale.timestamp,
            items = items,
            payments = payments,
            eventId = sale.eventId
        )
    }

    override suspend fun upsertSale(sale: Sale) {
        database.transaction {
            saleQueries.insertSale(sale.saleId, sale.timestamp, sale.eventId)
            
            sale.items.forEach { item ->
                saleQueries.insertSaleItem(
                    saleId = sale.saleId,
                    offerId = item.offerId,
                    description = item.description,
                    quantity = item.quantity.toLong(),
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice,
                    taxPercentage = item.taxPercentage,
                    inventoryAdjusted = if (item.inventoryAdjusted) 1L else 0L
                )
            }
            
            sale.payments.forEach { payment ->
                saleQueries.insertPaymentPart(
                    saleId = sale.saleId,
                    method = payment.method,
                    amount = payment.amount,
                    qrCodeData = payment.qrCodeData,
                    status = payment.status,
                    note = payment.note
                )
            }
        }
    }

    override suspend fun deleteSale(id: String) {
        saleQueries.deleteSale(id)
    }

    override suspend fun deleteAll() {
        saleQueries.deleteAllSales()
    }
}
