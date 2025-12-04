package com.bsobat.inventra.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.config.usecase.ObserveCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyNameUseCase
import com.bsobat.inventra.config.usecase.ObserveEventNameUseCase
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.sale.usecases.GetSaleByIdUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReceiptViewModel(
    private val getSaleByIdUseCase: GetSaleByIdUseCase,
    private val pdfExporter: PdfExporter,
    observeCompanyNameUseCase: ObserveCompanyNameUseCase,
    observeCompanyLogoUseCase: ObserveCompanyLogoUseCase,
    observeCompanyDescriptionUseCase: ObserveCompanyDescriptionUseCase,
    observeEventNameUseCase: ObserveEventNameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptUiState>(ReceiptUiState.Loading)
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()
    private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val pdfExportState: StateFlow<PdfExportState> = _pdfExportState.asStateFlow()

    val configData: StateFlow<ConfigData> = combine(
        observeCompanyNameUseCase(),
        observeCompanyLogoUseCase(),
        observeCompanyDescriptionUseCase(),
        observeEventNameUseCase()
    ) { name, logo, description, eventName ->
        ConfigData(
            companyName = name,
            companyLogo = logo,
            companyDescription = description,
            eventName = eventName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConfigData(null, null, null, null)
    )

    fun loadSale(saleId: String) {
        viewModelScope.launch {
            _pdfExportState.value = PdfExportState.Idle
            _uiState.value = ReceiptUiState.Loading
            val sale = getSaleByIdUseCase(saleId)
            _uiState.value = if (sale != null) {
                ReceiptUiState.Success(sale)
            } else {
                ReceiptUiState.Error("Sale not found")
            }
        }
    }

    fun exportToPdf() {
        val currentState = _uiState.value
        if (currentState !is ReceiptUiState.Success) return
        viewModelScope.launch {
            val config = configData.value

            _pdfExportState.value = PdfExportState.Exporting
            pdfExporter.exportReceiptToPdf(currentState.sale, configData = config)
                .onSuccess { file ->
                    _pdfExportState.value = PdfExportState.Success(file.absolutePath)
                }
                .onFailure { error ->
                    _pdfExportState.value = PdfExportState.Error(
                        error.message ?: "Failed to export PDF"
                    )
                }
        }
    }
}

sealed interface ReceiptUiState {
    data object Loading : ReceiptUiState
    data class Success(val sale: Sale) : ReceiptUiState
    data class Error(val message: String) : ReceiptUiState
}

sealed interface PdfExportState {
    object Idle : PdfExportState
    object Exporting : PdfExportState
    data class Success(val filePath: String) : PdfExportState
    data class Error(val message: String) : PdfExportState
}

data class ConfigData(
    val companyName: String?,
    val companyLogo: String?,
    val companyDescription: String?,
    val eventName: String?
)

