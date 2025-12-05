package com.bsobat.inventra.ui.config

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.config.usecase.ObserveCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyNameUseCase
import com.bsobat.inventra.config.usecase.ObserveEventNameUseCase
import com.bsobat.inventra.config.usecase.ObservePincodeUseCase
import com.bsobat.inventra.config.usecase.Pincode
import com.bsobat.inventra.config.usecase.UpdateCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyNameUseCase
import com.bsobat.inventra.config.usecase.UpdateEventNameUseCase
import com.bsobat.inventra.config.usecase.UpdatePincodeUseCase
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.PathProvider
import com.bsobat.inventra.domain.usecase.copyImageToInternalStorageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    observeCompanyNameUseCase: ObserveCompanyNameUseCase,
    observeCompanyLogoUseCase: ObserveCompanyLogoUseCase,
    observeCompanyDescriptionUseCase: ObserveCompanyDescriptionUseCase,
    observeEventNameUseCase: ObserveEventNameUseCase,
    observePincodeUseCase: ObservePincodeUseCase,
    private val updateCompanyNameUseCase: UpdateCompanyNameUseCase,
    private val updateCompanyLogoUseCase: UpdateCompanyLogoUseCase,
    private val updateCompanyDescriptionUseCase: UpdateCompanyDescriptionUseCase,
    private val updateEventNameUseCase: UpdateEventNameUseCase,
    private val updatePincodeUseCase: UpdatePincodeUseCase,
    private val contextProvider: ContextProvider,

    ) : ViewModel() {

    private val _editableData = MutableStateFlow(ConfigurationData(null, "", null, "", ""))
    val editableData: StateFlow<ConfigurationData> = _editableData

    val uiState: StateFlow<ConfigurationUiState> = combine(
        observePincodeUseCase(),
        observeCompanyNameUseCase(),
        observeCompanyLogoUseCase(),
        observeCompanyDescriptionUseCase(),
        observeEventNameUseCase()
    ) { pin, name, logo, description, event ->
        ConfigurationData(
            pincode = pin,
            companyName = name.orEmpty(),
            companyLogo = logo.orEmpty(),
            companyDescription = description.orEmpty(),
            eventName = event.orEmpty()
        )
    }
        .map<ConfigurationData, ConfigurationUiState> { data ->
            _editableData.value = data
            ConfigurationUiState.Success(data)
        }
        .onStart { emit(ConfigurationUiState.Loading) }
        .catch { emit(ConfigurationUiState.Error(it.message ?: "Unable to load configuration")) }
        .stateIn(viewModelScope, SharingStarted.Lazily, ConfigurationUiState.Loading)

    fun updateCompanyName(value: String) {
        _editableData.update { it.copy(companyName = value) }
    }

    fun updateCompanyLogo(value: Uri?) {

        viewModelScope.launch {
            _editableData.update {
                val imgPath = copyImageToInternalStorage("company", value?.toString())
                it.copy(companyLogo = imgPath)
            }
        }
    }

    private suspend fun copyImageToInternalStorage(imgName: String, imagePath: String?): String? {
        if (imagePath.isNullOrEmpty()) return null
        val provider = object : PathProvider {
            override fun invoke(): Any {
                return imagePath.toUri()
            }
        }
        return copyImageToInternalStorageUseCase(
            contextProvider,
            provider,
            "$imgName.jpg"
        )
    }

    fun updateCompanyDescription(value: String) {
        _editableData.update { it.copy(companyDescription = value) }
    }

    fun updateEventName(value: String) {
        _editableData.update { it.copy(eventName = value) }
    }

    fun updatePincode(value: Pincode) {
        _editableData.update { it.copy(pincode = value) }
    }

    fun saveConfiguration() {
        viewModelScope.launch {
            val data = _editableData.value
            updatePincodeUseCase(data.pincode)
            updateCompanyNameUseCase(data.companyName)
            updateCompanyLogoUseCase(data.companyLogo)
            updateCompanyDescriptionUseCase(data.companyDescription)
            updateEventNameUseCase(data.eventName)
        }
    }
}


sealed interface ConfigurationUiState {
    data object Loading : ConfigurationUiState
    data class Success(val data: ConfigurationData) : ConfigurationUiState
    data class Error(val message: String) : ConfigurationUiState
}

data class ConfigurationData(
    val pincode: Pincode?,
    val companyName: String,
    val companyLogo: String?,
    val companyDescription: String,
    val eventName: String
)