package com.bsobat.inventra.sale.usecases

import com.bsobat.inventra.data.repository.SaleRepository

class DeleteAllSalesUseCase(
    private val repository: SaleRepository
) {
    suspend operator fun invoke() {
        repository.deleteAll()
    }
}
