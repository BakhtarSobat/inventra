package com.bsobat.inventra.manager.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncMetadata(
    val lastSyncTimestamp: String,
    val exportTimestamp: String,
    val deviceId: String,
    val version: Int = 1
)