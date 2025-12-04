package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.manager.CheckoutManager

class GetPaymentMethodUseCase(
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke(methodId: String): PaymentMethod? {
        return checkoutManager.getPaymentMethod(methodId)
    }
}
