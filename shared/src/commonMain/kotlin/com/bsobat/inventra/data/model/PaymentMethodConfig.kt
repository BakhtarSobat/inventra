package com.bsobat.inventra.data.model

data class PaymentMethodConfig(
    val methodId: String,
    val name: String,
    val type: String,
    val enabled: Boolean,
    val configData: String? = null
)