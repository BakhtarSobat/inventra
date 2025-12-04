package com.bsobat.inventra.ui.basket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.basket.usecase.ObserveBasketItemsUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketSubtotalUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTaxUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTotalUseCase
import com.bsobat.inventra.basket.usecase.RemoveBasketItemUseCase
import com.bsobat.inventra.basket.usecase.UpdateBasketItemQuantityUseCase
import com.bsobat.inventra.data.model.BasketItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BasketViewModel(
    observeItems: ObserveBasketItemsUseCase,
    private val updateQuantity: UpdateBasketItemQuantityUseCase,
    private val removeItem: RemoveBasketItemUseCase,
    observeSubtotal: ObserveBasketSubtotalUseCase,
    observeTax: ObserveBasketTaxUseCase,
    observeTotal: ObserveBasketTotalUseCase
) : ViewModel() {

    val items: StateFlow<List<BasketItem>> = observeItems().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val subtotal: StateFlow<Double> = observeSubtotal().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )
    val tax: StateFlow<Double> = observeTax().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )
    val total: StateFlow<Double> = observeTotal().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )

    fun changeQuantity(offerId: String, quantity: Int) {
        viewModelScope.launch { updateQuantity(offerId, quantity) }
    }

    fun remove(offerId: String) {
        viewModelScope.launch { removeItem(offerId) }
    }
}
