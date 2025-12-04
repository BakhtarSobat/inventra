package com.bsobat.inventra.data.repository

import com.bsobat.inventra.data.model.PaymentMethodConfig
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    suspend fun observePaymentMethods(): Flow<List<PaymentMethodConfig>>
    suspend fun getPaymentMethod(methodId: String): PaymentMethodConfig?
    suspend fun savePaymentMethod(config: PaymentMethodConfig)
    suspend fun updateEnabled(methodId: String, enabled: Boolean)
    suspend fun deletePaymentMethod(methodId: String)
}