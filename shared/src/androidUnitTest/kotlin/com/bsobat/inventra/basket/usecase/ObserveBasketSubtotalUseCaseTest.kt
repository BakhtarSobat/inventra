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

class ObserveBasketSubtotalUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: ObserveBasketSubtotalUseCase

    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = ObserveBasketSubtotalUseCase(repository)
    }

    @Test
    fun `invoke should return flow of subtotal from repository`() = runTest {
        // Given
        val subtotal = 150.50
        every { repository.subtotal } returns flowOf(subtotal)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(subtotal, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return zero when basket is empty`() = runTest {
        // Given
        every { repository.subtotal } returns flowOf(0.0)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(0.0, awaitItem())
            awaitComplete()
        }
    }
}
