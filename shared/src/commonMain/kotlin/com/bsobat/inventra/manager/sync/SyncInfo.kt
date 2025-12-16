package com.bsobat.inventra.manager.sync

data class SyncInfo(
    val lastSyncTimestamp: String,
    val lastExportTimestamp: String,
    val deviceId: String,
    val hasRemoteBackup: Boolean
)