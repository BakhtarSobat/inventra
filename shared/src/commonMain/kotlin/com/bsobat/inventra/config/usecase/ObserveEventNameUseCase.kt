package com.bsobat.inventra.config.usecase

import kotlinx.coroutines.flow.Flow

class ObserveEventNameUseCase(
    private val observeConfigUseCase: ObserveConfigUseCase
) {
    operator fun invoke(): Flow<String?> = observeConfigUseCase(UpdateEventNameUseCase.Companion.KEY_EVENT)


}
