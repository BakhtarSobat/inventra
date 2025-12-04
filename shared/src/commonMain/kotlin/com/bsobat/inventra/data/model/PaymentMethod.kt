package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable


@Serializable
sealed class PaymentMethod {
    abstract val id: String
    abstract val name: String
    abstract val enabled: Boolean
    abstract val type: PaymentMethodType

    data class Cash(
        override val id: String,
        override val name: String,
        override val enabled: Boolean = true,
        override val type: PaymentMethodType = PaymentMethodType.CASH,
        var config: Online.Config? = null,
    ) : PaymentMethod() {
        @Serializable
        data class Config(
            var name: String,
            var description: String
        )
    }

    data class Online(
        override val id: String ,
        override val name: String ,
        override val enabled: Boolean = true,
        override val type: PaymentMethodType = PaymentMethodType.ONLINE,
        var config: Config? = null,
    ) : PaymentMethod() {
        companion object{
            const val TIKKIE_DESCRIPTION = "Inventra Payment Request"
            const val TIKKIE_PAYMENT_URL = "tikkie://payment_request?totalAmountInCents=%d&description=%s"
        }

        @Serializable
        data class Config(
            var name: String = "",
            var baseUrl: String,
            var description: String = ""
        )

        fun getPaymentUrl(amount: Double, description: String? = null): String {
            val amountInCents = (amount * 100).toInt()
            val baseUrl = config?.baseUrl
                ?: TIKKIE_PAYMENT_URL
            val amountParam = amountInCents.toLong().toString()
            return baseUrl
                .replace("%d", amountParam)
                .replace("%s", description?: config?.description ?: TIKKIE_DESCRIPTION)
        }
    }
}

enum class PaymentMethodType {
    CASH,
    ONLINE,
    CARD
}