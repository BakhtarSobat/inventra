package com.bsobat.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bsobat.inventra.data.model.Offer
import com.inventra.database.InventraDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import inventra.Offer as OfferEntity

class OfferRepositoryImpl(
    private val database: InventraDatabase,
    private val dispatcher: CoroutineDispatcher
) : OfferRepository {

    private val queries = database.offerQueries

    override fun observeAll(): Flow<List<Offer>> {
        return queries.selectAllOffers()
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeById(offerId: String): Flow<Offer?> {
        return queries.selectOfferById(offerId)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    override fun observeByProduct(productId: String): Flow<List<Offer>> {
        return queries.selectOffersByProduct(productId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeLowStock(threshold: Int): Flow<List<Offer>> {
        return queries.selectLowStockOffers(threshold.toLong())
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun add(offer: Offer) {
        withContext(dispatcher) {
            val entity = offer.toEntity()
            queries.insertOffer(
                offerId = entity.offerId,
                productId = entity.productId,
                title = entity.title,
                image = entity.image,
                amountInInventory = entity.amountInInventory,
                type = entity.type,
                price = entity.price,
                uom = entity.uom,
                taxPercentage = entity.taxPercentage
            )
        }
    }

    override suspend fun update(offer: Offer) {
        withContext(dispatcher) {
            val entity = offer.toEntity()
            queries.updateOffer(
                title = entity.title,
                image = entity.image,
                amountInInventory = entity.amountInInventory,
                type = entity.type,
                price = entity.price,
                uom = entity.uom,
                taxPercentage = entity.taxPercentage,
                offerId = entity.offerId
            )
        }
    }

    override suspend fun updateInventory(offerId: String, newAmount: Int) {
        withContext(dispatcher) {
            queries.updateInventory(newAmount.toLong(), offerId)
        }
    }

    override suspend fun delete(offerId: String) {
        withContext(dispatcher) {
            queries.deleteOffer(offerId)
        }
    }

    override suspend fun getById(offerId: String): Offer? = withContext(dispatcher) {
        queries.selectOfferById(offerId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun search(query: String): List<Offer> = withContext(dispatcher) {
        queries.searchOffers("%$query%").executeAsList().map { it.toDomain() }
    }
}

fun OfferEntity.toDomain(): Offer {
    return Offer(
        offerId = offerId,
        productId = productId,
        title = title,
        image = image,
        amountInInventory = amountInInventory.toInt(),
        type = type,
        price = price,
        uom = uom,
        taxPercentage = taxPercentage
    )
}

fun Offer.toEntity(): OfferEntity {
    return OfferEntity(
        offerId = offerId,
        productId = productId,
        title = title,
        image = image,
        amountInInventory = amountInInventory.toLong(),
        type = type,
        price = price,
        uom = uom,
        taxPercentage = taxPercentage
    )
}