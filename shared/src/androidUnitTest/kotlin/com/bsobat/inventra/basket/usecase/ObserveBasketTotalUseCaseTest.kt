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

class ObserveBasketTotalUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: ObserveBasketTotalUseCase

    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = ObserveBasketTotalUseCase(repository)
    }

    @Test
    fun `invoke should return flow of total from repository`() = runTest {
        // Given
        val total = 176.25
        every { repository.total } returns flowOf(total)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(total, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return zero when basket is empty`() = runTest {
        // Given
        every { repository.total } returns flowOf(0.0)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(0.0, awaitItem())
            awaitComplete()
        }
    }
}
