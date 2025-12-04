package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.PaymentMethodConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.data.model.PaymentMethodType
import com.inventra.database.InventraDatabase
import inventra.Payment_method_config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path

class PaymentMethodRepositoryImpl(
    private val database: InventraDatabase
) : PaymentMethodRepository {

    private val queries = database.paymentMethodQueries

    override suspend fun observePaymentMethods(): Flow<List<PaymentMethodConfig>> {

        checkIfDatabaseIsEmpty()
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toDomain() }
            }
    }

    private suspend fun checkIfDatabaseIsEmpty() {
        // Synchronously check if any payment methods exist
        if (queries.selectAll().executeAsList().isEmpty()) {
            // Add your default payment methods here
            val defaults = listOf(
                PaymentMethod.Cash("default", "Cash", true),
            ).map {
                PaymentMethodConfig(
                    methodId = it.id,
                    name = it.name,
                    enabled = it.enabled,
                    type = when (it.type) {
                        PaymentMethodType.CASH -> "CASH"
                        PaymentMethodType.ONLINE -> "ONLINE"
                        PaymentMethodType.CARD -> "CARD"
                    },
                    configData = null
                )
            }
            defaults.forEach { savePaymentMethod(it) }
        }
    }

    override suspend fun getPaymentMethod(methodId: String): PaymentMethodConfig? =
        queries.selectById(methodId)
            .executeAsOneOrNull()
            ?.toDomain()

    override suspend fun savePaymentMethod(config: PaymentMethodConfig) {
        queries.insertOrUpdate(
            methodId = config.methodId,
            name = config.name,
            enabled = if (config.enabled) 1L else 0L,
            configData = config.configData,
            type = config.type
        )
    }

    override suspend fun updateEnabled(methodId: String, enabled: Boolean) {
        queries.updateEnabled(
            enabled = if (enabled) 1L else 0L,
            methodId = methodId
        )
    }

    override suspend fun deletePaymentMethod(methodId: String) {
        queries.deleteById(methodId)
    }

    private fun Payment_method_config.toDomain() =
        PaymentMethodConfig(
            methodId = methodId,
            name = name,
            enabled = enabled != 0L,
            configData = configData,
            type = type
        )
}


