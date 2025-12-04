package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.manager.CheckoutManager

class ProcessCheckoutUseCase(
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke(
        basketItems: List<BasketItem>,
        payments: List<PaymentPart>,
        eventId: String? = null
    ): Result<String> {
        return checkoutManager.processCheckout(basketItems, payments, eventId)
    }
}
