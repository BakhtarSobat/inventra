package com.bsobat.inventra.ui

import androidx.lifecycle.ViewModel
import com.bsobat.inventra.basket.usecase.ObserveBasketItemsUseCase
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.manager.DataExportImportManager

class MainActivityViewModel(
    private val dataExportImportManager: DataExportImportManager,
    observeBasketItemsUseCase: ObserveBasketItemsUseCase,
    private val adminPinCheckUseCase: AdminPinCheckUseCase,

    ) : ViewModel() {
    val adminLoggedIn = adminPinCheckUseCase.adminLoggedIn

    val basketItemsFlow = observeBasketItemsUseCase()
    suspend fun export(path: String) = dataExportImportManager.exportToZip(path)
    suspend fun import(path: String) = dataExportImportManager.importFromZip(path)
    fun onAdminPinCheck(pin: String) = adminPinCheckUseCase(pin)


}