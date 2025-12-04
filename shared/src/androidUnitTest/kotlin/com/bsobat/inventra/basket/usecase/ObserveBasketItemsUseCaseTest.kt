package com.bsobat.inventra.basket.usecase

import app.cash.turbine.test
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.repository.BasketRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ObserveBasketItemsUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: ObserveBasketItemsUseCase

    @BeforeTest
    fun setup() {
        repository = mockk()
        useCase = ObserveBasketItemsUseCase(repository)
    }

    @Test
    fun `invoke should return flow of basket items from repository`() = runTest {
        // Given
        val basketItems = listOf(
            mockk<BasketItem>(),
            mockk<BasketItem>()
        )
        every { repository.items } returns flowOf(basketItems)

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(basketItems, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke should return empty list when basket is empty`() = runTest {
        // Given
        every { repository.items } returns flowOf(emptyList())

        // When
        val flow = useCase()

        // Then
        flow.test {
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
    }
}
