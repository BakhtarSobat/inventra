package com.bsobat.inventra.ui.offer

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.basket.usecase.AddBasketItemUseCase
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.PathProvider
import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.domain.usecase.copyImageToInternalStorageUseCase
import com.bsobat.inventra.offer.domain.usecase.AddOfferUseCase
import com.bsobat.inventra.offer.domain.usecase.DeleteOfferUseCase
import com.bsobat.inventra.offer.domain.usecase.GetOfferByIdUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveAllOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveLowStockOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveOffersByProductUseCase
import com.bsobat.inventra.offer.domain.usecase.SearchOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.UpdateOfferInventoryUseCase
import com.bsobat.inventra.offer.domain.usecase.UpdateOfferUseCase
import com.bsobat.inventra.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface OffersUiState {
    data object Loading : OffersUiState
    data class Success(val offers: List<Offer>, val searchQuery: String = "") : OffersUiState
    data class Error(val message: String) : OffersUiState
}

class OfferViewModel(
    private val observeAllOffersUseCase: ObserveAllOffersUseCase,
    private val observeOffersByProductUseCase: ObserveOffersByProductUseCase,
    private val observeLowStockOffersUseCase: ObserveLowStockOffersUseCase,
    private val addOfferUseCase: AddOfferUseCase,
    private val updateOfferUseCase: UpdateOfferUseCase,
    private val updateOfferInventoryUseCase: UpdateOfferInventoryUseCase,
    private val deleteOfferUseCase: DeleteOfferUseCase,
    private val getOfferByIdUseCase: GetOfferByIdUseCase,
    private val searchOffersUseCase: SearchOffersUseCase,
    private val adminPinCheckUseCase: AdminPinCheckUseCase,
    private val basketItemUseCase: AddBasketItemUseCase,
    private val contextProvider: ContextProvider,
    ) : ViewModel() {
    private val _uiState = MutableStateFlow<OffersUiState>(OffersUiState.Loading)
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()

    private val _selectedOffer = MutableStateFlow<Offer?>(null)
    val selectedOffer: StateFlow<Offer?> = _selectedOffer.asStateFlow()
    val adminLoggedIn = adminPinCheckUseCase.adminLoggedIn

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()



    fun loadOffersByProduct(inputParams: Screen.OffersScreen) {
        viewModelScope.launch {
            _uiState.value = OffersUiState.Loading
            observeOffersByProductUseCase(inputParams.productId)
                .catch { e ->
                    _uiState.value = OffersUiState.Error(e.message ?: "Failed to load offers")
                }
                .collect { offers ->
                    _uiState.value = OffersUiState.Success(offers.sortedBy { it.title })
                }
        }
    }

    fun addOffer(offer: Offer, product: Screen.OffersScreen) {
        viewModelScope.launch {
            try {
                offer.image = copyImageToInternalStorage(offer, product)
                addOfferUseCase(offer)
                loadOffersByProduct(product)
            } catch (e: Exception) {
                _uiState.value = OffersUiState.Error(e.message ?: "Failed to add offer")
            }
        }
    }


    fun updateOffer(offer: Offer, product: Screen.OffersScreen) {
        viewModelScope.launch {
            try {
                offer.image = copyImageToInternalStorage(offer, product)
                updateOfferUseCase(offer)
                loadOffersByProduct(product)
            } catch (e: Exception) {
                _uiState.value = OffersUiState.Error(e.message ?: "Failed to update offer")
            }
        }
    }

    fun updateInventory(offerId: String, newAmount: Int) {
        viewModelScope.launch {
            try {
                updateOfferInventoryUseCase(offerId, newAmount)
            } catch (e: Exception) {
                _uiState.value = OffersUiState.Error(e.message ?: "Failed to update inventory")
            }
        }
    }

    fun deleteOffer(offer: Offer, product: Screen.OffersScreen) {
        viewModelScope.launch {
            try {
                deleteOfferUseCase(offer.offerId)
                loadOffersByProduct(product)
            } catch (e: Exception) {
                _uiState.value = OffersUiState.Error(e.message ?: "Failed to delete offer")
            }
        }
    }

    fun getOfferById(offerId: String) {
        viewModelScope.launch {
            try {
                val offer = getOfferByIdUseCase(offerId)
                _selectedOffer.value = offer
            } catch (e: Exception) {
                _uiState.value = OffersUiState.Error(e.message ?: "Failed to get offer")
            }
        }
    }

    fun searchOffers(query: String) {
        viewModelScope.launch {
            _uiState.value = OffersUiState.Loading
            try {
                val searchResults = searchOffersUseCase(query)
                _uiState.value = OffersUiState.Success(searchResults, query)
            } catch (e: Exception) {
                _uiState.value = OffersUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun loadLowStockOffers(threshold: Int = 10) {
        viewModelScope.launch {
            _uiState.value = OffersUiState.Loading
            observeLowStockOffersUseCase(threshold)
                .catch { e ->
                    _uiState.value =
                        OffersUiState.Error(e.message ?: "Failed to load low stock offers")
                }
                .collect { offers ->
                    _uiState.value = OffersUiState.Success(offers)
                }
        }
    }


    private suspend fun copyImageToInternalStorage(offer: Offer, product: Screen.OffersScreen): String? {
        val imagePath = offer.image
        if (imagePath.isNullOrEmpty()) return product.productImage
        val provider = object : PathProvider {
            override fun invoke(): Any {
                return imagePath.toUri()
            }
        }
        return copyImageToInternalStorageUseCase(
            contextProvider,
            provider,
            "offer_${product.productId}.jpg"
        )
    }
    fun onAdminPinCheck(pin: String){
        viewModelScope.launch {
            adminPinCheckUseCase(pin)
        }
    }

    fun clearSelectedOffer() {
        _selectedOffer.value = null
    }

    fun addToBasket(offer: Offer, quantity: Int) {
        basketItemUseCase(offer, quantity)
    }
}