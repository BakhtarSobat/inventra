package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.manager.CheckoutManager

class ValidatePaymentsUseCase(
    private val checkoutManager: CheckoutManager
) {
    operator fun invoke(payments: List<PaymentPart>): Boolean {
        return checkoutManager.validatePayments(payments)
    }
}
