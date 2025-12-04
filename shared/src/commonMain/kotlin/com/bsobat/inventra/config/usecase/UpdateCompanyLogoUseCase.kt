package com.bsobat.inventra.config.usecase

class UpdateCompanyLogoUseCase(
    private val updateConfigUseCase: UpdateConfigUseCase
) {
    suspend operator fun invoke(value: String?) {
        updateConfigUseCase(KEY, value)
    }

    private companion object {
        private const val KEY = "company_logo"
    }
}
