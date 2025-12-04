package com.bsobat.inventra.checkout.usecase

import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.manager.CheckoutManager

class SavePaymentMethodConfigUseCase(
    private val checkoutManager: CheckoutManager
) {
    suspend operator fun invoke(
        paymentMethod: PaymentMethod,
    ) {
        val configData  = when(paymentMethod) {
            is PaymentMethod.Cash -> emptyMap()
            is PaymentMethod.Online -> paymentMethod.config?.let { config ->
                mapOf(
                    "baseUrl" to config.baseUrl,
                    "name" to config.name,
                    "description" to config.description,
                )
            }?: emptyMap()
        }
        checkoutManager.savePaymentMethodConfig(paymentMethod.id, paymentMethod.name, paymentMethod.type.name, paymentMethod.enabled, configData)
    }
}
