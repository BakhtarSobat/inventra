package com.bsobat.inventra.config.usecase

import com.bsobat.inventra.data.repository.ConfigurationRepository

class UpdateConfigUseCase(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(key: String, value: String?) {
        if(value == null) {
            repository.remove(key)
            return
        }
        repository.upsert(key, value)
    }
}
