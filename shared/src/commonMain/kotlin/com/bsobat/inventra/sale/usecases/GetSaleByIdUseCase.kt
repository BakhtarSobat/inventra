package com.bsobat.inventra.sale.usecases

import com.bsobat.inventra.data.repository.SaleRepository
import com.bsobat.inventra.data.model.Sale

class GetSaleByIdUseCase(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(id: String): Sale? {
        return repository.getSaleById(id)
    }
}
