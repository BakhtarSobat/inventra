package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.manager.CheckoutManager

class DeletePaymentMethodUseCase (
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke(
        paymentMethod: PaymentMethod,
    ) {
        checkoutManager.deletePaymentMethod(paymentMethod)
    }
}
