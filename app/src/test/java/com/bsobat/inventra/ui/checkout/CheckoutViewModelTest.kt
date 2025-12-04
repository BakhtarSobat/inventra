package com.bsobat.inventra.ui.checkout

import app.cash.turbine.test
import com.bsobat.inventra.basket.usecase.ClearBasketUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketItemsUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketSubtotalUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTaxUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTotalUseCase
import com.bsobat.inventra.checkout.usecase.DeletePaymentMethodUseCase
import com.bsobat.inventra.checkout.usecase.ObservePaymentMethodsUseCase
import com.bsobat.inventra.checkout.usecase.ProcessCheckoutUseCase
import com.bsobat.inventra.checkout.usecase.SavePaymentMethodConfigUseCase
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private lateinit var observeBasketItems: ObserveBasketItemsUseCase
    private lateinit var observeBasketSubtotal: ObserveBasketSubtotalUseCase
    private lateinit var observeBasketTax: ObserveBasketTaxUseCase
    private lateinit var observeBasketTotal: ObserveBasketTotalUseCase
    private lateinit var observePaymentMethods: ObservePaymentMethodsUseCase
    private lateinit var processCheckout: ProcessCheckoutUseCase
    private lateinit var savePaymentMethodConfig: SavePaymentMethodConfigUseCase
    private lateinit var adminPinCheck: AdminPinCheckUseCase
    private lateinit var clearBasket: ClearBasketUseCase
    private lateinit var deletePaymentMethod: DeletePaymentMethodUseCase
    private lateinit var viewModel: CheckoutViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        observeBasketItems = mockk()
        observeBasketSubtotal = mockk()
        observeBasketTax = mockk()
        observeBasketTotal = mockk()
        observePaymentMethods = mockk()
        processCheckout = mockk()
        savePaymentMethodConfig = mockk(relaxed = true)
        adminPinCheck = mockk(relaxed = true)
        clearBasket = mockk(relaxed = true)
        deletePaymentMethod = mockk(relaxed = true)

        every { observeBasketItems() } returns flowOf(emptyList())
        every { observeBasketSubtotal() } returns flowOf(0.0)
        every { observeBasketTax() } returns flowOf(0.0)
        every { observeBasketTotal() } returns flowOf(0.0)
        coEvery { observePaymentMethods() } returns flowOf(emptyList())
        every { adminPinCheck.adminLoggedIn } returns MutableStateFlow(false)

        viewModel = createViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load should emit Loading then Success with basket data`() = runTest {
        // Given
        val basketItems = listOf(mockk<BasketItem>(relaxed = true))
        val subtotal = 100.0
        val tax = 10.0
        val total = 110.0

        every { observeBasketItems() } returns flowOf(basketItems)
        every { observeBasketSubtotal() } returns flowOf(subtotal)
        every { observeBasketTax() } returns flowOf(tax)
        every { observeBasketTotal() } returns flowOf(total)

        viewModel = createViewModel()

        // When
        val uiState = viewModel.load()
        advanceUntilIdle()

        // Then
        uiState.test {
            val successState = awaitItem() as CheckoutUiState.Success
            assertEquals(basketItems, successState.basketItems)
            assertEquals(subtotal, successState.subtotal)
            assertEquals(tax, successState.tax)
            assertEquals(total, successState.total)
            assertNull(successState.selectedPaymentMethod)
            assertFalse(successState.isProcessing)
        }
    }

    @Test
    fun `load should update paymentMethodState with enabled payment methods`() = runTest {
        // Given
        val cashMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)
        val disabledMethod = PaymentMethod.Cash(id = "disabled", name = "Disabled", enabled = false)
        val enabledOnline = PaymentMethod.Online(
            id = "online",
            name = "Online",
            enabled = true,
            config = null
        )

        coEvery { observePaymentMethods() } returns flowOf(listOf(cashMethod, disabledMethod, enabledOnline))

        viewModel = createViewModel()

        // When
        viewModel.load()
        advanceUntilIdle()

        // Then
        viewModel.paymentMethodState.test {
            val state = awaitItem() as PaymentMethodUiState.Success
            assertEquals(2, state.paymentMethods.size)
            assertTrue(state.paymentMethods.all { it.enabled })
        }
    }

    @Test
    fun `onEvent SelectPaymentMethod should update selected payment method`() = runTest {
        // Given
        val paymentMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)
        viewModel.load()
        advanceUntilIdle()

        // When
        viewModel.onEvent(CheckoutEvent.SelectPaymentMethod(paymentMethod))
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem() as CheckoutUiState.Success
            assertEquals(paymentMethod, state.selectedPaymentMethod)
        }
    }

    @Test
    fun `onEvent ProcessCheckout should process checkout with cash payment`() = runTest {
        // Given
        val basketItems = listOf(mockk<BasketItem>(relaxed = true))
        val total = 110.0
        val saleId = "sale-123"
        val cashMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)

        every { observeBasketItems() } returns flowOf(basketItems)
        every { observeBasketTotal() } returns flowOf(total)
        coEvery { processCheckout(any(), any(), any()) } returns Result.success(saleId)
        coEvery { clearBasket() } just Runs

        viewModel = createViewModel()
        viewModel.load()
        advanceUntilIdle()

        viewModel.onEvent(CheckoutEvent.SelectPaymentMethod(cashMethod))
        advanceUntilIdle()

        // When
        viewModel.onEvent(CheckoutEvent.ProcessCheckout)

        // Then
        viewModel.effect.test {
            val effect = awaitItem() as CheckoutEffect.CheckoutSuccess
            assertEquals(saleId, effect.saleId)
        }
        coVerify(exactly = 1) { clearBasket() }
    }

    @Test
    fun `onEvent ProcessCheckout should process checkout with online payment`() = runTest {
        // Given
        val basketItems = listOf(mockk<BasketItem>(relaxed = true))
        val total = 110.0
        val saleId = "sale-123"
        val onlineMethod = PaymentMethod.Online(
            id = "online",
            name = "Online",
            enabled = true,
            config = PaymentMethod.Online.Config(
                baseUrl = "https://pay.example.com?amount=%d&desc=%s",
            )
        )

        every { observeBasketItems() } returns flowOf(basketItems)
        every { observeBasketTotal() } returns flowOf(total)
        coEvery { processCheckout(any(), any(), any()) } returns Result.success(saleId)
        coEvery { clearBasket() } just Runs

        viewModel = createViewModel()
        viewModel.load()
        advanceUntilIdle()

        viewModel.onEvent(CheckoutEvent.SelectPaymentMethod(onlineMethod))
        advanceUntilIdle()

        // When
        viewModel.onEvent(CheckoutEvent.ProcessCheckout)

        // Then
        viewModel.effect.test {
            val effect = awaitItem() as CheckoutEffect.CheckoutOnlinePaymentRequired
            assertEquals(saleId, effect.saleId)
            assertTrue(effect.paymentUrl.isNotEmpty())
        }
        coVerify(exactly = 1) { clearBasket() }
    }

    @Test
    fun `onEvent ProcessCheckout should emit error when checkout fails`() = runTest {
        // Given
        val basketItems = listOf(mockk<BasketItem>(relaxed = true))
        val errorMessage = "Checkout failed"
        val cashMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)

        every { observeBasketItems() } returns flowOf(basketItems)
        coEvery { processCheckout(any(), any(), any()) } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        viewModel.load()
        advanceUntilIdle()

        viewModel.onEvent(CheckoutEvent.SelectPaymentMethod(cashMethod))
        advanceUntilIdle()

        // When
        viewModel.onEvent(CheckoutEvent.ProcessCheckout)

        // Then
        viewModel.effect.test {
            val effect = awaitItem() as CheckoutEffect.CheckoutError
            assertEquals(errorMessage, effect.message)
        }
        coVerify(exactly = 0) { clearBasket() }
    }

    @Test
    fun `onEvent ProcessCheckout should not process when no payment method selected`() = runTest {
        // Given
        val basketItems = listOf(mockk<BasketItem>(relaxed = true))
        every { observeBasketItems() } returns flowOf(basketItems)

        viewModel = createViewModel()
        viewModel.load()
        advanceUntilIdle()

        // When
        viewModel.onEvent(CheckoutEvent.ProcessCheckout)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { processCheckout(any(), any(), any()) }
    }

    @Test
    fun `onEvent ProcessCheckout should not process when basket is empty`() = runTest {
        // Given
        val cashMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)
        every { observeBasketItems() } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.load()
        advanceUntilIdle()

        viewModel.onEvent(CheckoutEvent.SelectPaymentMethod(cashMethod))
        advanceUntilIdle()

        // When
        viewModel.onEvent(CheckoutEvent.ProcessCheckout)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { processCheckout(any(), any(), any()) }
    }

    @Test
    fun `savePaymentMethod should call savePaymentMethodConfig`() = runTest {
        // Given
        val paymentMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)
        coEvery { savePaymentMethodConfig(any()) } just Runs

        // When
        viewModel.savePaymentMethod(paymentMethod)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { savePaymentMethodConfig(paymentMethod) }
    }


    @Test
    fun `deletePaymentMethod should call deletePaymentMethodUseCase`() = runTest {
        // Given
        val paymentMethod = PaymentMethod.Cash(id = "cash", name = "Cash", enabled = true)
        coEvery { deletePaymentMethod(any()) } just Runs

        // When
        viewModel.deletePaymentMethod(paymentMethod)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { deletePaymentMethod(paymentMethod) }
    }

    @Test
    fun `adminLoggedIn should return flow from adminPinCheck`() = runTest {
        // Given
        val adminFlow = MutableStateFlow(true)
        every { adminPinCheck.adminLoggedIn } returns adminFlow

        // When
        viewModel = createViewModel()

        // Then
        viewModel.adminLoggedIn.test {
            assertEquals(true, awaitItem())
        }
    }

    private fun createViewModel() = CheckoutViewModel(
        observeBasketItems,
        observeBasketSubtotal,
        observeBasketTax,
        observeBasketTotal,
        observePaymentMethods,
        processCheckout,
        savePaymentMethodConfig,
        adminPinCheck,
        clearBasket,
        deletePaymentMethod
    )
}
