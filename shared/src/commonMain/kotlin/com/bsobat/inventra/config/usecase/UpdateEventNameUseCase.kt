package com.bsobat.inventra.config.usecase

class UpdateEventNameUseCase(
    private val updateConfigUseCase: UpdateConfigUseCase
) {
    suspend operator fun invoke(value: String) {
        updateConfigUseCase(KEY_EVENT, value)
    }

    internal companion object {
        internal const val KEY_EVENT = "event_name"
    }
}
