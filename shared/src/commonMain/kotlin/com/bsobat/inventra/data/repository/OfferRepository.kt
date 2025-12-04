package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.Offer
import kotlinx.coroutines.flow.Flow

interface OfferRepository {
    fun observeAll(): Flow<List<Offer>>
    fun observeById(offerId: String): Flow<Offer?>
    fun observeByProduct(productId: String): Flow<List<Offer>>
    fun observeLowStock(threshold: Int): Flow<List<Offer>>
    suspend fun add(offer: Offer)
    suspend fun update(offer: Offer)
    suspend fun updateInventory(offerId: String, newAmount: Int)
    suspend fun delete(offerId: String)
    suspend fun getById(offerId: String): Offer?
    suspend fun search(query: String): List<Offer>
}
