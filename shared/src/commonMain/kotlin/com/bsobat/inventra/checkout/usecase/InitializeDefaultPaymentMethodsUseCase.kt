package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.manager.CheckoutManager

class InitializeDefaultPaymentMethodsUseCase(
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke() {
        checkoutManager.initializeDefaultMethods()
    }
}
