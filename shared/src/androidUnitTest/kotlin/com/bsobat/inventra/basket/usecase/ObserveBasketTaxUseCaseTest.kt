package com.bsobat.inventra.basket.usecase

import app.cash.turbine.test
import com.bsobat.inventra.data.repository.BasketRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ObserveBasketTaxUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: ObserveBasketTaxUseCase

    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = ObserveBasketTaxUseCase(repository)
    }

    @Test
    fun `invoke should return flow of tax from repository`() = runTest {
        // Given
        val tax = 25.75
        every { repository.tax } returns flowOf(tax)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(tax, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return zero when basket is empty`() = runTest {
        // Given
        every { repository.tax } returns flowOf(0.0)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(0.0, awaitItem())
            awaitComplete()
        }
    }
}
