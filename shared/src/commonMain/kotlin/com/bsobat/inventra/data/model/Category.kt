package com.bsobat.inventra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val categoryId: String,
    val title: String,
    val description: String?,
    var image: String?,
    val taxPercentage: Double
)