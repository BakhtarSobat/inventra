package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.manager.CheckoutManager

class EnablePaymentMethodUseCase(
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke(methodId: String, enabled: Boolean) {
        checkoutManager.enablePaymentMethod(methodId, enabled)
    }
}
