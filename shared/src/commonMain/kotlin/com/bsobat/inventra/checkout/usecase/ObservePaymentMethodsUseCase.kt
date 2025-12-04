package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.manager.CheckoutManager
import kotlinx.coroutines.flow.Flow

class ObservePaymentMethodsUseCase(
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke(): Flow<List<PaymentMethod>> {
        return checkoutManager.observePaymentMethods()
    }
}
