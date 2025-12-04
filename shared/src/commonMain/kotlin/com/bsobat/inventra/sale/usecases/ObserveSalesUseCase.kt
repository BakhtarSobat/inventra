package com.bsobat.inventra.sale.usecases

import com.bsobat.inventra.data.repository.SaleRepository
import com.bsobat.inventra.data.model.Sale
import kotlinx.coroutines.flow.Flow

class ObserveSalesUseCase(
    private val repository: SaleRepository
) {
    operator fun invoke(): Flow<List<Sale>> {
        return repository.observeSales()
    }
}
