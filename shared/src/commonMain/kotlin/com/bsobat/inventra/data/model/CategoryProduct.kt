package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryProduct(
    val categoryId: String,
    val productId: String
)