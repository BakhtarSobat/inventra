package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.BeforeTest

class RemoveBasketItemUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: RemoveBasketItemUseCase

    @BeforeTest
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = RemoveBasketItemUseCase(repository)
    }

    @Test
    fun `invoke should call repository remove with offerId`() {
        // Given
        val offerId = "offer-123"
        every { repository.remove(offerId) } returns Unit

        // When
        useCase(offerId)

        // Then
        verify(exactly = 1) { repository.remove(offerId) }
    }
}
