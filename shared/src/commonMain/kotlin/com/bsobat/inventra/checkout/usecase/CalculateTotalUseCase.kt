package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.manager.CheckoutManager

class CalculateTotalUseCase(
    private val checkoutManager: CheckoutManager
) {
    operator fun invoke(basketItems: List<BasketItem>): Double {
        return checkoutManager.calculateTotal(basketItems)
    }
}
