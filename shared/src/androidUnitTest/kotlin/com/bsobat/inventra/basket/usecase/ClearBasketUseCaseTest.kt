package com.bsobat.inventra.basket.usecase

import com.bsobat.inventra.data.repository.BasketRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.BeforeTest

class ClearBasketUseCaseTest {

    private lateinit var repository: BasketRepository
    private lateinit var useCase: ClearBasketUseCase

    @BeforeTest
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = ClearBasketUseCase(repository)
    }

    @Test
    fun `invoke should call repository clear`() {
        // Given
        every { repository.clear() } returns Unit

        // When
        useCase()

        // Then
        verify(exactly = 1) { repository.clear() }
    }
}
