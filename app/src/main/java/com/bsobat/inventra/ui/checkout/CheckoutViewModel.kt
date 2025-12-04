package com.bsobat.inventra.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.basket.usecase.ClearBasketUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketItemsUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketSubtotalUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTaxUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTotalUseCase
import com.bsobat.inventra.checkout.usecase.DeletePaymentMethodUseCase
import com.bsobat.inventra.checkout.usecase.ObservePaymentMethodsUseCase
import com.bsobat.inventra.checkout.usecase.ProcessCheckoutUseCase
import com.bsobat.inventra.checkout.usecase.SavePaymentMethodConfigUseCase
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface CheckoutUiState {
    data object Loading : CheckoutUiState
    data class Success(
        val basketItems: List<BasketItem>,
        val subtotal: Double,
        val tax: Double,
        val total: Double,
        val selectedPaymentMethod: PaymentMethod?,
        val isProcessing: Boolean
    ) : CheckoutUiState

    data class Error(val message: String) : CheckoutUiState
}

sealed interface PaymentMethodUiState {
    data class Success(val paymentMethods: List<PaymentMethod>) : PaymentMethodUiState
    data class Error(val message: String) : PaymentMethodUiState
}

private data class CheckoutData(
    val basketItems: List<BasketItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double
)

sealed interface CheckoutEvent {
    data class SelectPaymentMethod(val paymentMethod: PaymentMethod) : CheckoutEvent
    data object ProcessCheckout : CheckoutEvent
    data object ClearError : CheckoutEvent
}

sealed interface CheckoutEffect {
    data class CheckoutSuccess(val saleId: String) : CheckoutEffect
    data class CheckoutOnlinePaymentRequired(val saleId: String, val paymentUrl: String) :
        CheckoutEffect

    data class CheckoutError(val message: String) : CheckoutEffect
}

class CheckoutViewModel(
    private val observeBasketItemsUseCase: ObserveBasketItemsUseCase,
    private val observeBasketSubtotalUseCase: ObserveBasketSubtotalUseCase,
    private val observeBasketTaxUseCase: ObserveBasketTaxUseCase,
    private val observeBasketTotalUseCase: ObserveBasketTotalUseCase,
    private val observePaymentMethodsUseCase: ObservePaymentMethodsUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val savePaymentMethodConfigUseCase: SavePaymentMethodConfigUseCase,
    private val adminPinCheckUseCase: AdminPinCheckUseCase,
    private val clearBasketUseCase: ClearBasketUseCase,
    private val deletePaymentMethodUseCase: DeletePaymentMethodUseCase,
) : ViewModel() {

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    private val _isProcessing = MutableStateFlow(false)
    private val _effect = MutableSharedFlow<CheckoutEffect>()
    val adminLoggedIn: StateFlow<Boolean> = adminPinCheckUseCase.adminLoggedIn
    private val _uiState: MutableStateFlow<CheckoutUiState> =
        MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _paymentMethodState: MutableStateFlow<PaymentMethodUiState> =
        MutableStateFlow(PaymentMethodUiState.Success(emptyList()))
    val paymentMethodState: StateFlow<PaymentMethodUiState> = _paymentMethodState.asStateFlow()

    val effect: SharedFlow<CheckoutEffect> = _effect.asSharedFlow()

    fun load(): StateFlow<CheckoutUiState> {
        viewModelScope.launch {
            observePaymentMethodsUseCase().collect { paymentMethods ->
                _paymentMethodState.value =
                PaymentMethodUiState.Success(paymentMethods.filter { it.enabled })
            }
        }
        viewModelScope.launch {
            combine(
                combine(
                    observeBasketItemsUseCase(),
                    observeBasketSubtotalUseCase(),
                    observeBasketTaxUseCase(),
                    observeBasketTotalUseCase()
                ) { basketItems, subtotal, tax, total ->
                    CheckoutData(basketItems, subtotal, tax, total)
                },
                _selectedPaymentMethod,
                _isProcessing
            ) { checkoutData, selectedMethod, isProcessing ->
                CheckoutUiState.Success(
                    basketItems = checkoutData.basketItems,
                    subtotal = checkoutData.subtotal,
                    tax = checkoutData.tax,
                    total = checkoutData.total,
                    selectedPaymentMethod = selectedMethod,
                    isProcessing = isProcessing
                )
            }.catch { error ->
                CheckoutUiState.Error(error.message ?: "Unknown error")
            }.collect {
                _uiState.value = it
            }
        }
        return uiState
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.SelectPaymentMethod -> {
                _selectedPaymentMethod.value = event.paymentMethod
            }

            is CheckoutEvent.ProcessCheckout -> {
                processCheckout()
            }

            is CheckoutEvent.ClearError -> {
                // Error clearing handled by UI
            }
        }
    }

    fun savePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            savePaymentMethodConfigUseCase(paymentMethod)
        }
    }

    private fun processCheckout() {
        val currentState = uiState.value
        if (currentState !is CheckoutUiState.Success) return

        val selectedMethod = currentState.selectedPaymentMethod ?: return
        if (currentState.basketItems.isEmpty()) return

        viewModelScope.launch {
            _isProcessing.value = true
            val payment = PaymentPart(
                method = selectedMethod.id,
                amount = currentState.total,
                status = "COMPLETED"
            )

            processCheckoutUseCase(
                basketItems = currentState.basketItems,
                payments = listOf(payment),
                eventId = null
            ).fold(
                onSuccess = { saleId ->
                    val value = when (selectedMethod) {
                        is PaymentMethod.Cash -> CheckoutEffect.CheckoutSuccess(saleId)
                        is PaymentMethod.Online -> CheckoutEffect.CheckoutOnlinePaymentRequired(
                            saleId = saleId,
                            paymentUrl = selectedMethod.getPaymentUrl(currentState.total)
                        )
                    }
                    _effect.emit(value)
                    clearBasketUseCase()
                },
                onFailure = { exception ->
                    _effect.emit(
                        CheckoutEffect.CheckoutError(
                            exception.message ?: "Checkout failed"
                        )
                    )
                }
            )

            _isProcessing.value = false
        }
    }

    fun onAdminPinCheck(pin: String) {
        adminPinCheckUseCase(pin)
    }

    fun deletePaymentMethod(payment: PaymentMethod) {
        viewModelScope.launch {
            deletePaymentMethodUseCase.invoke(payment)
        }
    }
}