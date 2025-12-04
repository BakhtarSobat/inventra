package com.bsobat.inventra.ui.basket

import app.cash.turbine.test
import com.bsobat.inventra.basket.usecase.*
import com.bsobat.inventra.data.model.BasketItem
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class BasketViewModelTest {

    private lateinit var observeItems: ObserveBasketItemsUseCase
    private lateinit var updateQuantity: UpdateBasketItemQuantityUseCase
    private lateinit var removeItem: RemoveBasketItemUseCase
    private lateinit var observeSubtotal: ObserveBasketSubtotalUseCase
    private lateinit var observeTax: ObserveBasketTaxUseCase
    private lateinit var observeTotal: ObserveBasketTotalUseCase
    private lateinit var viewModel: BasketViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        observeItems = mockk()
        updateQuantity = mockk(relaxed = true)
        removeItem = mockk(relaxed = true)
        observeSubtotal = mockk()
        observeTax = mockk()
        observeTotal = mockk()

        every { observeItems() } returns flowOf(emptyList())
        every { observeSubtotal() } returns flowOf(0.0)
        every { observeTax() } returns flowOf(0.0)
        every { observeTotal() } returns flowOf(0.0)

        viewModel = BasketViewModel(
            observeItems,
            updateQuantity,
            removeItem,
            observeSubtotal,
            observeTax,
            observeTotal
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `items should emit basket items from use case`() = runTest {
        // Given
        val basketItems = listOf(
            mockk<BasketItem>(relaxed = true),
            mockk<BasketItem>(relaxed = true)
        )
        every { observeItems() } returns flowOf(basketItems)

        // When
        val newViewModel = BasketViewModel(
            observeItems,
            updateQuantity,
            removeItem,
            observeSubtotal,
            observeTax,
            observeTotal
        )

        // Then
        newViewModel.items.test {
            assertEquals(emptyList(), awaitItem())
            assertEquals(basketItems, awaitItem())
        }
    }

    @Test
    fun `items should emit empty list when basket is empty`() = runTest {
        // When
        viewModel.items.test {
            // Then
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `subtotal should emit value from use case`() = runTest {
        // Given
        val expectedSubtotal = 150.50
        every { observeSubtotal() } returns flowOf(expectedSubtotal)

        // When
        val newViewModel = BasketViewModel(
            observeItems,
            updateQuantity,
            removeItem,
            observeSubtotal,
            observeTax,
            observeTotal
        )

        // Then
        newViewModel.subtotal.test {
            assertEquals(0.0, awaitItem())
            assertEquals(expectedSubtotal, awaitItem())
        }
    }

    @Test
    fun `tax should emit value from use case`() = runTest {
        // Given
        val expectedTax = 25.75
        every { observeTax() } returns flowOf(expectedTax)

        // When
        val newViewModel = BasketViewModel(
            observeItems,
            updateQuantity,
            removeItem,
            observeSubtotal,
            observeTax,
            observeTotal
        )

        // Then
        newViewModel.tax.test {
            assertEquals(0.0, awaitItem())
            assertEquals(expectedTax, awaitItem())
        }
    }

    @Test
    fun `total should emit value from use case`() = runTest {
        // Given
        val expectedTotal = 176.25
        every { observeTotal() } returns flowOf(expectedTotal)

        // When
        val newViewModel = BasketViewModel(
            observeItems,
            updateQuantity,
            removeItem,
            observeSubtotal,
            observeTax,
            observeTotal
        )
        // Then
        newViewModel.total.test {
            assertEquals(0.0, awaitItem())
            assertEquals(expectedTotal, awaitItem())
        }
    }

    @Test
    fun `changeQuantity should call updateQuantity use case`() = runTest {
        // Given
        val offerId = "offer-123"
        val quantity = 5
        coEvery { updateQuantity(offerId, quantity) } just Runs

        // When
        viewModel.changeQuantity(offerId, quantity)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { updateQuantity(offerId, quantity) }
    }

    @Test
    fun `remove should call removeItem use case`() = runTest {
        // Given
        val offerId = "offer-123"
        coEvery { removeItem(offerId) } just Runs

        // When
        viewModel.remove(offerId)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { removeItem(offerId) }
    }

    @Test
    fun `changeQuantity with zero should still call use case`() = runTest {
        // Given
        val offerId = "offer-123"
        val quantity = 0
        coEvery { updateQuantity(offerId, quantity) } just Runs

        // When
        viewModel.changeQuantity(offerId, quantity)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { updateQuantity(offerId, quantity) }
    }

    @Test
    fun `multiple changeQuantity calls should invoke use case for each`() = runTest {
        // Given
        val offerId1 = "offer-1"
        val offerId2 = "offer-2"
        coEvery { updateQuantity(any(), any()) } just Runs

        // When
        viewModel.changeQuantity(offerId1, 3)
        viewModel.changeQuantity(offerId2, 5)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { updateQuantity(offerId1, 3) }
        coVerify(exactly = 1) { updateQuantity(offerId2, 5) }
    }
}
