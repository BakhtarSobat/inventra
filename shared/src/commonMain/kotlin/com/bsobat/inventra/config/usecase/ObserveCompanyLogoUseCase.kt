package com.bsobat.inventra.config.usecase

import com.bsobat.inventra.config.usecase.UpdateCompanyLogoUseCase.Companion.KEY_LOGO
import kotlinx.coroutines.flow.Flow

class ObserveCompanyLogoUseCase(
    private val observeConfigUseCase: ObserveConfigUseCase
) {
    operator fun invoke(): Flow<String?> = observeConfigUseCase(KEY_LOGO)

}
