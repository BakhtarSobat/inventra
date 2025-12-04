package com.bsobat.inventra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import inventra.Configuration
import inventra.ConfigurationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class ConfigurationRepositoryImpl(
    private val configurationQueries: ConfigurationQueries,
    scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ConfigurationRepository {

    private val state = MutableStateFlow<Map<String, String>>(emptyMap())
    override val configs: Flow<Map<String, String>> = state.asStateFlow()

    init {
        configurationQueries
            .allConfig()
            .asFlow()
            .mapToList(dispatcher)
            .onEach { rows -> state.value = rows.toMap() }
            .launchIn(scope)
    }

    override fun config(key: String): Flow<String?> =
        configs.map { it[key] }

    override suspend fun upsert(key: String, value: String): Unit =
        withContext(dispatcher) {
            configurationQueries.insertOrReplaceConfig(key, value)
        }

    override suspend fun remove(key: String): Unit =
        withContext(dispatcher) {
            configurationQueries.deleteConfig(key)
        }

    override suspend fun clear() =
        withContext(dispatcher) {
            configurationQueries.transaction {
                configurationQueries
                    .allConfig()
                    .executeAsList()
                    .forEach { row: Configuration ->
                        configurationQueries.deleteConfig(row.key)
                    }
            }
        }

    private fun List<Configuration>.toMap(): Map<String, String> =
        associate { it.key to it.value_ }
}
