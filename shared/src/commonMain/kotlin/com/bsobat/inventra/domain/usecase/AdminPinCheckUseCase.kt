package com.bsobat.inventra.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminPinCheckUseCase {
    private val _adminLoggedIn = MutableStateFlow(false)
    val adminLoggedIn: StateFlow<Boolean> = _adminLoggedIn.asStateFlow()

    operator fun invoke(inputPin: String) {
        _adminLoggedIn.value = inputPin == "1234"
    }
}