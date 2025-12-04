package com.bsobat.inventra.data.repository

import kotlinx.coroutines.flow.Flow

interface ConfigurationRepository {
    val configs: Flow<Map<String, String>>

    fun config(key: String): Flow<String?>

    suspend fun upsert(key: String, value: String)

    suspend fun remove(key: String)

    suspend fun clear()
}
