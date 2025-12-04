package com.bsobat.inventra.ui.products

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.PathProvider
import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.domain.usecase.copyImageToInternalStorageUseCase
import com.bsobat.inventra.product.domain.usecase.AddProductUseCase
import com.bsobat.inventra.product.domain.usecase.DeleteProductUseCase
import com.bsobat.inventra.product.domain.usecase.GetAllProductsUseCase
import com.bsobat.inventra.product.domain.usecase.GetProductsByCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.RemoveProductFromCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.SearchProductsUseCase
import com.bsobat.inventra.product.domain.usecase.UpdateProductUseCase
import com.bsobat.inventra.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data class Success(val products: List<Product>, val searchQuery: String = "") : ProductsUiState
    data class Error(val message: String) : ProductsUiState
}

class ProductsViewModel(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val removeProductFromCategoryUseCase: RemoveProductFromCategoryUseCase,
    private val adminPinCheckUseCase: AdminPinCheckUseCase,
    private val contextProvider: ContextProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    val adminLoggedIn = adminPinCheckUseCase.adminLoggedIn

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    fun loadProducts(input: Screen.ProductsScreen) {
        viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading
            input.categoryId?.let { cat ->
                getProductsByCategoryUseCase(cat)
                    .catch { e -> _uiState.value = ProductsUiState.Error(e.message ?: "Unknown error") }
                    .collect { products ->
                        _uiState.value = ProductsUiState.Success(products.sortedBy { it.title })
                    }
            }?: getAllProductsUseCase()
                .catch { e -> _uiState.value = ProductsUiState.Error(e.message ?: "Unknown error") }
                .collect { products ->
                    _uiState.value = ProductsUiState.Success(products.sortedBy { it.title })
                }

        }
    }

    fun addProduct(product: Product, inputParams: Screen.ProductsScreen) {
        viewModelScope.launch {
            if(inputParams.categoryId == null) {
                _uiState.value = ProductsUiState.Error("Please select a category to add product")
                return@launch
            }
            try {
                product.image = copyImageToInternalStorage(product, inputParams)
                addProductUseCase(product, inputParams.categoryId)
                loadProducts(inputParams)
            } catch (e: Exception) {
                _uiState.value = ProductsUiState.Error(e.message ?: "Failed to add product")
            }
        }
    }

    fun editProduct(product: Product, category: Screen.ProductsScreen) {
        viewModelScope.launch {
            try {
                product.image = copyImageToInternalStorage(product, category)
                updateProductUseCase(product)
                loadProducts(category)
            } catch (e: Exception) {
                _uiState.value = ProductsUiState.Error(e.message ?: "Failed to update product")
            }
        }
    }

    private suspend fun copyImageToInternalStorage(product: Product, inputParams: Screen.ProductsScreen): String? {
        val imagePath = product.image
        if (imagePath.isNullOrEmpty()) return inputParams.categoryImage
        val provider = object : PathProvider {
            override fun invoke(): Any {
                return imagePath.toUri()
            }
        }
        return copyImageToInternalStorageUseCase(
            contextProvider,
            provider,
            "product_${product.productId}.jpg"
        )
    }

    fun deleteProduct(product: Product, inputParams: Screen.ProductsScreen) {
        viewModelScope.launch {
            try {
                deleteProductUseCase(product.productId)
                inputParams.categoryId?.let {
                    removeProductFromCategoryUseCase(product.productId, inputParams.categoryId)
                }
                loadProducts(inputParams)
            } catch (e: Exception) {
                _uiState.value = ProductsUiState.Error(e.message ?: "Failed to delete product")
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading
            searchProductsUseCase(query)
                .catch { e -> _uiState.value = ProductsUiState.Error(e.message ?: "Search failed") }
                .collect { products ->
                    _uiState.value = ProductsUiState.Success(products, query)
                }
        }
    }


    fun onAdminPinCheck(pin: String) = adminPinCheckUseCase(pin)

    fun clearPinError() {
        _pinError.value = false
    }
}