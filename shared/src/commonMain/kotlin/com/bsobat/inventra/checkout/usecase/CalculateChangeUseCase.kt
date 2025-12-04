package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.manager.CheckoutManager

class CalculateChangeUseCase(
    private val checkoutManager: CheckoutManager
) {
    operator fun invoke(basketItems: List<BasketItem>, payments: List<PaymentPart>): Double {
        return checkoutManager.calculateChange(basketItems, payments)
    }
}
