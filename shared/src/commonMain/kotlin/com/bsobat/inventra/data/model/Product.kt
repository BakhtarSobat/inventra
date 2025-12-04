package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val productId: String,
    val title: String,
    val description: String?,
    var image: String?,
    val skuCode: String? = null,
    val barcode: String? = null
)