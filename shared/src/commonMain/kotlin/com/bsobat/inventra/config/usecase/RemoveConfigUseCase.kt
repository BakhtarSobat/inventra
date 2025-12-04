package com.bsobat.inventra.config.usecase

import com.bsobat.inventra.data.repository.ConfigurationRepository

class RemoveConfigUseCase(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(key: String) {
        repository.remove(key)
    }
}
