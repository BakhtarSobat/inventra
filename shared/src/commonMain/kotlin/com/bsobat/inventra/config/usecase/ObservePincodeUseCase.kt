package com.bsobat.inventra.config.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.jvm.JvmInline

@JvmInline value class Pincode(val value: String)
class ObservePincodeUseCase(
    private val observeConfigUseCase: ObserveConfigUseCase
) {
    operator fun invoke(): Flow<Pincode?> = observeConfigUseCase(UpdatePincodeUseCase.Companion.KEY_PIN).map {
        it?.let {
            Pincode(it)
        }
    }

}
