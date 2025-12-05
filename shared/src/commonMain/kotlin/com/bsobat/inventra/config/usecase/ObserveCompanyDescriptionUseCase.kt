package com.bsobat.inventra.config.usecase

import com.bsobat.inventra.config.usecase.UpdateCompanyDescriptionUseCase.Companion.KEY_COMPANY_DESCRIPTION
import kotlinx.coroutines.flow.Flow

class ObserveCompanyDescriptionUseCase(
    private val observeConfigUseCase: ObserveConfigUseCase
) {
    operator fun invoke(): Flow<String?> = observeConfigUseCase(KEY_COMPANY_DESCRIPTION)
}
