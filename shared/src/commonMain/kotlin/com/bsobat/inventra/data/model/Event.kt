package com.bsobat.inventra.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class Event @OptIn(ExperimentalTime::class) constructor(
    val eventId: String,
    val title: String,
    val description: String?,
    val startDate: Instant,
    val endDate: Instant
)