package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.repository.BasketRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.BeforeTest

class AddBasketItemUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: AddBasketItemUseCase

    @BeforeTest
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = AddBasketItemUseCase(repository)
    }

    @Test
    fun `invoke should call repository add with offer and quantity`() {
        // Given
        val offer = mockk<Offer>()
        val quantity = 3

        every { repository.add(offer, quantity) } returns Unit

        // When
        useCase(offer, quantity)

        // Then
        verify(exactly = 1) { repository.add(offer, quantity) }
    }

    @Test
    fun `invoke should use default quantity of 1 when not specified`() {
        // Given
        val offer = mockk<Offer>()

        every { repository.add(offer, 1) } returns Unit

        // When
        useCase(offer)

        // Then
        verify(exactly = 1) { repository.add(offer, 1) }
    }
}
