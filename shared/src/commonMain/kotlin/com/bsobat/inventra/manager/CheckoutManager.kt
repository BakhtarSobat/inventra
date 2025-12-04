package com.bsobat.inventra.manager

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.InventoryTransaction
import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.data.model.PaymentMethodConfig
import com.bsobat.inventra.data.model.PaymentMethodType
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.data.model.SaleItem
import com.bsobat.inventra.data.repository.InventoryTransactionRepository
import com.bsobat.inventra.data.repository.PaymentMethodRepository
import com.bsobat.inventra.data.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CheckoutManager(
    private val saleRepository: SaleRepository,
    private val inventoryTransactionRepository: InventoryTransactionRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {

    suspend fun observePaymentMethods(): Flow<List<PaymentMethod>> =
        paymentMethodRepository.observePaymentMethods().map { configs ->
            configs.mapNotNull { it.toPaymentMethod() }
        }

    suspend fun deletePaymentMethod(payment: PaymentMethod) {
        paymentMethodRepository.deletePaymentMethod(payment.id)
    }

    suspend fun getPaymentMethod(methodId: String): PaymentMethod? =
        paymentMethodRepository.getPaymentMethod(methodId)?.toPaymentMethod()

    suspend fun savePaymentMethodConfig(
        methodId: String,
        name: String,
        type: String,
        enabled: Boolean = true,
        configData: Map<String, Any?>? = null
    ) {
        val serializedConfig = configData?.let {
            Json.encodeToString(kotlinx.serialization.json.buildJsonObject {
                it.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, kotlinx.serialization.json.JsonPrimitive(value))
                        is Number -> put(key, kotlinx.serialization.json.JsonPrimitive(value))
                        is Boolean -> put(key, kotlinx.serialization.json.JsonPrimitive(value))
                        null -> put(key, kotlinx.serialization.json.JsonNull)
                    }
                }
            })
        }

        val config = PaymentMethodConfig(
            methodId = methodId,
            name = name,
            type = type,
            enabled = enabled,
            configData = serializedConfig
        )

        paymentMethodRepository.savePaymentMethod(config)
    }

    suspend fun enablePaymentMethod(methodId: String, enabled: Boolean) {
        paymentMethodRepository.updateEnabled(methodId, enabled)
    }

    suspend fun initializeDefaultMethods() {
        val defaultMethods = listOf(
            PaymentMethodConfig("cash", "Cash", "CASH", true, null),
            PaymentMethodConfig("tikkie", "Tikkie", "ONLINE", false, null)
        )

        defaultMethods.forEach { method ->
            if (paymentMethodRepository.getPaymentMethod(method.methodId) == null) {
                paymentMethodRepository.savePaymentMethod(method)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun processCheckout(
        basketItems: List<BasketItem>,
        payments: List<PaymentPart>,
        eventId: String? = null
    ): Result<String> {
        return try {
            val timestamp = Clock.System.now().epochSeconds.toString()
            val saleId = timestamp

            val saleItems = basketItems.map { basketItem ->
                SaleItem(
                    offerId = basketItem.offer.offerId,
                    description = basketItem.offer.title,
                    quantity = basketItem.quantity,
                    unitPrice = basketItem.offer.price,
                    totalPrice = basketItem.offer.price * basketItem.quantity,
                    taxPercentage = 0.0,
                    inventoryAdjusted = false
                )
            }

            val totalAmount = saleItems.sumOf { it.totalPrice }
            val paidAmount = payments.sumOf { it.amount }

            if (paidAmount < totalAmount) {
                return Result.failure(Exception("Insufficient payment: paid $paidAmount, required $totalAmount"))
            }

            val sale = Sale(
                saleId = saleId,
                timestamp = timestamp,
                items = saleItems,
                payments = payments,
                eventId = eventId
            )

            saleRepository.upsertSale(sale)

            saleItems.forEach { item ->
                val transaction = InventoryTransaction(
                    transactionId = "${timestamp}_${item.offerId}",
                    offerId = item.offerId,
                    changeAmount = -item.quantity,
                    reason = "SALE",
                    timestamp = timestamp,
                    eventId = eventId
                )

                inventoryTransactionRepository.recordTransaction(transaction)
            }

            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateTotal(basketItems: List<BasketItem>): Double {
        return basketItems.sumOf { it.offer.price * it.quantity }
    }

    fun calculateChange(basketItems: List<BasketItem>, payments: List<PaymentPart>): Double {
        val total = calculateTotal(basketItems)
        val paid = payments.sumOf { it.amount }
        return maxOf(0.0, paid - total)
    }

    fun validatePayments(payments: List<PaymentPart>): Boolean {
        return payments.all { it.status == "COMPLETED" || it.status == "SUCCESS" }
    }

    private fun PaymentMethodConfig.toPaymentMethod(): PaymentMethod? {
        val ptype = PaymentMethodType.entries.firstOrNull { it.name.equals(type, ignoreCase = true) } ?: return null
        return when (ptype) {
            PaymentMethodType.CASH -> PaymentMethod.Cash(id = methodId, name = name, enabled = enabled)
            PaymentMethodType.ONLINE -> {
                val config =
                    configData?.let { Json.decodeFromString<PaymentMethod.Online.Config>(it) }
                PaymentMethod.Online(
                    id = methodId,
                    name = name,
                    enabled = enabled,
                    config = config
                )
            }

            else -> null
        }
    }
}