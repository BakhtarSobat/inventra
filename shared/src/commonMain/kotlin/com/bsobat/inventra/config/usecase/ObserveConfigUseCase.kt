package com.bsobat.inventra.config.usecase

import com.bsobat.inventra.data.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow

class ObserveConfigUseCase(
    private val repository: ConfigurationRepository
) {
    operator fun invoke(key: String): Flow<String?> = repository.config(key)
}
