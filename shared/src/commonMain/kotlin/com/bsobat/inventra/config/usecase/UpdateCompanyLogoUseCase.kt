package com.bsobat.inventra.config.usecase

class UpdateCompanyLogoUseCase(
    private val updateConfigUseCase: UpdateConfigUseCase
) {
    suspend operator fun invoke(value: String?) {
        updateConfigUseCase(KEY_LOGO, value)
    }

    internal companion object {
        const val KEY_LOGO = "company_logo"
    }
}
