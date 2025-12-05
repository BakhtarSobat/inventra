package com.bsobat.inventra.config.usecase

import com.bsobat.inventra.config.usecase.UpdateCompanyNameUseCase.Companion.KEY_COMPANY_NAME
import kotlinx.coroutines.flow.Flow

class ObserveCompanyNameUseCase(
    private val observeConfigUseCase: ObserveConfigUseCase
) {
    operator fun invoke(): Flow<String?> = observeConfigUseCase(KEY_COMPANY_NAME)

}
