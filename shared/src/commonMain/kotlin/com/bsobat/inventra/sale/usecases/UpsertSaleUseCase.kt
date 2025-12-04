package com.bsobat.inventra.sale.usecases

import com.bsobat.inventra.data.repository.SaleRepository
import com.bsobat.inventra.data.model.Sale

class UpsertSaleUseCase(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(sale: Sale) {
        repository.upsertSale(sale)
    }
}
