package com.bsobat.inventra.sale.usecases

import com.bsobat.inventra.data.repository.SaleRepository

class DeleteSaleUseCase(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteSale(id)
    }
}
