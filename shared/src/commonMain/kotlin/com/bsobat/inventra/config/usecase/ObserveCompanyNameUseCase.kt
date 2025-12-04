package com.bsobat.inventra.config.usecase

import kotlinx.coroutines.flow.Flow

class ObserveCompanyNameUseCase(
    private val observeConfigUseCase: ObserveConfigUseCase
) {
    operator fun invoke(): Flow<String?> = observeConfigUseCase(KEY)

    private companion object {
        private const val KEY = "company_name"
    }
}
