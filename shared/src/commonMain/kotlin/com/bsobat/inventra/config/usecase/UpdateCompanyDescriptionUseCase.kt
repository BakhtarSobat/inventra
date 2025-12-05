package com.bsobat.inventra.config.usecase

class UpdateCompanyDescriptionUseCase(
    private val updateConfigUseCase: UpdateConfigUseCase
) {
    suspend operator fun invoke(value: String) {
        updateConfigUseCase(KEY_COMPANY_DESCRIPTION, value)
    }

    internal companion object {
        const val KEY_COMPANY_DESCRIPTION = "company_description"
    }
}
