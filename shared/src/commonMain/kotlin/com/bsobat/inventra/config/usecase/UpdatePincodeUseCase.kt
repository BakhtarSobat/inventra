package com.bsobat.inventra.config.usecase

class UpdatePincodeUseCase(
    private val updateConfigUseCase: UpdateConfigUseCase
) {
    suspend operator fun invoke(value: Pincode?) {
        updateConfigUseCase(KEY_PIN, value?.value)
    }

    internal companion object {
        internal const val KEY_PIN = "PINCODE"
    }
}
