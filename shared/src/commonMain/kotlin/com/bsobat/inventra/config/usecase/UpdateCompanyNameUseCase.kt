package com.bsobat.inventra.config.usecase

class UpdateCompanyNameUseCase(
    private val updateConfigUseCase: UpdateConfigUseCase
) {
    suspend operator fun invoke(value: String) {
        updateConfigUseCase(KEY_COMPANY_NAME, value)
    }

    internal companion object {
        internal const val KEY_COMPANY_NAME = "company_name"
    }
}
