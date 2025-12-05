package com.bsobat.inventra.ui.categories

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.category.domain.usecase.AddCategoryUseCase
import com.bsobat.inventra.category.domain.usecase.DeleteCategoryUseCase
import com.bsobat.inventra.category.domain.usecase.GetCategoriesUseCase
import com.bsobat.inventra.category.domain.usecase.UpdateCategoryUseCase
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.PathProvider
import com.bsobat.inventra.data.model.Category
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.domain.usecase.copyImageToInternalStorageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

sealed class CategoryUiState {
    data class Success(val categories: List<Category>) : CategoryUiState()
    data class Error(val message: String) : CategoryUiState()
    object Loading : CategoryUiState()
}

class CategoryViewModel(
    private val getCategories: GetCategoriesUseCase,
    private val addCategory: AddCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val contextProvider: ContextProvider,
    private val adminPinCheckUseCase: AdminPinCheckUseCase,
) : ViewModel() {

    val adminLoggedIn = adminPinCheckUseCase.adminLoggedIn
    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getCategories().collectLatest { list ->
                _uiState.value = CategoryUiState.Success(list)
            }
        }
    }

    fun createCategory(
        title: String,
        description: String?,
        image: String?,
        taxPercentage: Double
    ) {
        viewModelScope.launch {
            runCatching {
                val category = Category(
                    categoryId = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    image = image,
                    taxPercentage = taxPercentage
                )
                category.image = copyImageToInternalStorage(category)
                addCategory(category)
            }.onFailure {
                setError(it.message)
            }
        }
    }

    fun editCategory(updated: Category) {
        viewModelScope.launch {
            updated.image = copyImageToInternalStorage(updated)
            runCatching { updateCategory(updated) }
                .onFailure { setError(it.message) }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            runCatching {
                deleteCategory(category.categoryId)
            }.onFailure { setError(it.message) }
        }
    }

    private suspend fun copyImageToInternalStorage(category: Category): String? {
        val imagePath = category.image
        if (imagePath.isNullOrEmpty()) return null
        val provider = object : PathProvider {
            override fun invoke(): Any {
                return imagePath.toUri()
            }
        }
        return copyImageToInternalStorageUseCase(
            contextProvider,
            provider,
            "category_${category.categoryId}.jpg"
        )
    }

    private fun setError(msg: String?) {
        _uiState.value = CategoryUiState.Error(msg ?: "Unknown Error")
    }

    fun onAdminPinCheck(pin: String){
        viewModelScope.launch {
            adminPinCheckUseCase(pin)
        }
    }
}