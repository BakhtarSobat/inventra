package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.BeforeTest

class UpdateBasketItemQuantityUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: UpdateBasketItemQuantityUseCase

    @BeforeTest
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = UpdateBasketItemQuantityUseCase(repository)
    }

    @Test
    fun `invoke should call repository update with offerId and quantity`() {
        // Given
        val offerId = "offer-123"
        val quantity = 5
        every { repository.update(offerId, quantity) } returns Unit

        // When
        useCase(offerId, quantity)

        // Then
        verify(exactly = 1) { repository.update(offerId, quantity) }
    }

    @Test
    fun `invoke should handle zero quantity`() {
        // Given
        val offerId = "offer-123"
        val quantity = 0
        every { repository.update(offerId, quantity) } returns Unit

        // When
        useCase(offerId, quantity)

        // Then
        verify(exactly = 1) { repository.update(offerId, quantity) }
    }
}
