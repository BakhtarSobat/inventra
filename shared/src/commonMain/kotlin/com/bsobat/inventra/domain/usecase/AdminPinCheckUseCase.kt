package com.bsobat.inventra.domain.usecase

import com.bsobat.inventra.config.usecase.ObservePincodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class AdminPinCheckUseCase(private val observePincodeUseCase: ObservePincodeUseCase) {
    private val _adminLoggedIn = MutableStateFlow(false)
    val adminLoggedIn: StateFlow<Boolean> = _adminLoggedIn.asStateFlow()

    suspend operator fun invoke(inputPin: String) {
        observePincodeUseCase().map { storedPin ->
            inputPin == (storedPin?.value?:"0000")
        }.collect {
            _adminLoggedIn.value = it
        }
    }
}