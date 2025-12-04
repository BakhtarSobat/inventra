package com.bsobat.inventra.manager

import app.cash.turbine.test
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.data.model.PaymentMethodConfig
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.data.repository.InventoryTransactionRepository
import com.bsobat.inventra.data.repository.PaymentMethodRepository
import com.bsobat.inventra.data.repository.SaleRepository
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CheckoutManagerTest {

    private lateinit var checkoutManager: CheckoutManager
    private lateinit var saleRepository: SaleRepository
    private lateinit var inventoryTransactionRepository: InventoryTransactionRepository
    private lateinit var paymentMethodRepository: PaymentMethodRepository

    @BeforeTest
    fun setup() {
        saleRepository = mockk()
        inventoryTransactionRepository = mockk()
        paymentMethodRepository = mockk()

        checkoutManager = CheckoutManager(
            saleRepository = saleRepository,
            inventoryTransactionRepository = inventoryTransactionRepository,
            paymentMethodRepository = paymentMethodRepository
        )
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `observePaymentMethods should map configs to payment methods`() = runTest {
        // Given
        val cashConfig = PaymentMethodConfig("cash", "Cash", "CASH", true, null)
        val onlineConfig = PaymentMethodConfig("tikkie", "Tikkie", "ONLINE", true, null)
        coEvery { paymentMethodRepository.observePaymentMethods() } returns flowOf(
            listOf(cashConfig, onlineConfig)
        )

        // When
        checkoutManager.observePaymentMethods().test {
            val result = awaitItem()

            // Then
            assertEquals(2, result.size)
            assertTrue(result[0] is PaymentMethod.Cash)
            assertEquals("cash", result[0].id)
            assertEquals("Cash", result[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `deletePaymentMethod should call repository delete`() = runTest {
        // Given
        val payment = PaymentMethod.Cash("cash", "Cash", true)
        coEvery { paymentMethodRepository.deletePaymentMethod("cash") } just Runs

        // When
        checkoutManager.deletePaymentMethod(payment)

        // Then
        coVerify { paymentMethodRepository.deletePaymentMethod("cash") }
    }

    @Test
    fun `getPaymentMethod should return mapped payment method`() = runTest {
        // Given
        val config = PaymentMethodConfig("cash", "Cash", "CASH", true, null)
        coEvery { paymentMethodRepository.getPaymentMethod("cash") } returns config

        // When
        val result = checkoutManager.getPaymentMethod("cash")

        // Then
        assertNotNull(result)
        assertEquals("cash", result.id)
        assertEquals("Cash", result.name)
        assertTrue(result is PaymentMethod.Cash)
    }

    @Test
    fun `savePaymentMethodConfig should serialize config data correctly`() = runTest {
        // Given
        val configData = mapOf("apiKey" to "test123", "enabled" to true)
        coEvery { paymentMethodRepository.savePaymentMethod(any()) } just Runs

        // When
        checkoutManager.savePaymentMethodConfig(
            methodId = "tikkie",
            name = "Tikkie",
            type = "ONLINE",
            enabled = true,
            configData = configData
        )

        // Then
        coVerify {
            paymentMethodRepository.savePaymentMethod(
                match {
                    it.methodId == "tikkie" &&
                            it.name == "Tikkie" &&
                            it.type == "ONLINE" &&
                            it.enabled &&
                            it.configData != null
                }
            )
        }
    }

    @Test
    fun `enablePaymentMethod should update enabled status`() = runTest {
        // Given
        coEvery { paymentMethodRepository.updateEnabled("cash", false) } just Runs

        // When
        checkoutManager.enablePaymentMethod("cash", false)

        // Then
        coVerify { paymentMethodRepository.updateEnabled("cash", false) }
    }

    @Test
    fun `initializeDefaultMethods should create default payment methods`() = runTest {
        // Given
        coEvery { paymentMethodRepository.getPaymentMethod(any()) } returns null
        coEvery { paymentMethodRepository.savePaymentMethod(any()) } just Runs

        // When
        checkoutManager.initializeDefaultMethods()

        // Then
        coVerify(exactly = 2) { paymentMethodRepository.savePaymentMethod(any()) }
    }

    @Test
    fun `processCheckout should create sale and adjust inventory`() = runTest {
        // Given
        val offer = createTestOffer()
        val basketItems = listOf(BasketItem(offer, 2))
        val payments = listOf(PaymentPart("cash", 20.0, null, "COMPLETED", null))

        coEvery { saleRepository.upsertSale(any()) } just Runs
        coEvery { inventoryTransactionRepository.recordTransaction(any()) } just Runs

        // When
        val result = checkoutManager.processCheckout(basketItems, payments)

        // Then
        assertTrue(result.isSuccess)
        coVerify { saleRepository.upsertSale(any()) }
        coVerify { inventoryTransactionRepository.recordTransaction(any()) }
    }

    @Test
    fun `processCheckout should fail with insufficient payment`() = runTest {
        // Given
        val offer = createTestOffer(price = 10.0)
        val basketItems = listOf(BasketItem(offer, 2))
        val payments = listOf(PaymentPart("cash", 15.0, null, "COMPLETED", null))

        // When
        val result = checkoutManager.processCheckout(basketItems, payments)

        // Then
        assertTrue(result.isFailure)
        assertEquals(
            "Insufficient payment: paid 15.0, required 20.0",
            result.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { saleRepository.upsertSale(any()) }
    }

    @Test
    fun `processCheckout with eventId should include event in sale and transaction`() = runTest {
        // Given
        val offer = createTestOffer()
        val basketItems = listOf(BasketItem(offer, 1))
        val payments = listOf(PaymentPart("cash", 10.0, null, "COMPLETED", null))
        val eventId = "event123"

        coEvery { saleRepository.upsertSale(any()) } just Runs
        coEvery { inventoryTransactionRepository.recordTransaction(any()) } just Runs

        // When
        val result = checkoutManager.processCheckout(basketItems, payments, eventId)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            saleRepository.upsertSale(match { it.eventId == eventId })
            inventoryTransactionRepository.recordTransaction(match { it.eventId == eventId })
        }
    }

    @Test
    fun `calculateTotal should sum all basket items correctly`() {
        // Given
        val offer1 = createTestOffer(price = 10.0)
        val offer2 = createTestOffer(price = 15.0)
        val basketItems = listOf(
            BasketItem(offer1, 2), // 20.0
            BasketItem(offer2, 3)  // 45.0
        )

        // When
        val total = checkoutManager.calculateTotal(basketItems)

        // Then
        assertEquals(65.0, total)
    }

    @Test
    fun `calculateChange should return correct change amount`() {
        // Given
        val offer = createTestOffer(price = 10.0)
        val basketItems = listOf(BasketItem(offer, 2)) // 20.0 total
        val payments = listOf(PaymentPart("cash", 50.0, null, "COMPLETED", null))

        // When
        val change = checkoutManager.calculateChange(basketItems, payments)

        // Then
        assertEquals(30.0, change)
    }

    @Test
    fun `calculateChange should return zero when payment equals total`() {
        // Given
        val offer = createTestOffer(price = 10.0)
        val basketItems = listOf(BasketItem(offer, 2))
        val payments = listOf(PaymentPart("cash", 20.0, null, "COMPLETED", null))

        // When
        val change = checkoutManager.calculateChange(basketItems, payments)

        // Then
        assertEquals(0.0, change)
    }

    @Test
    fun `calculateChange should return zero when underpaid`() {
        // Given
        val offer = createTestOffer(price = 10.0)
        val basketItems = listOf(BasketItem(offer, 2))
        val payments = listOf(PaymentPart("cash", 15.0, null, "COMPLETED", null))

        // When
        val change = checkoutManager.calculateChange(basketItems, payments)

        // Then
        assertEquals(0.0, change)
    }

    @Test
    fun `validatePayments should return true for all completed payments`() {
        // Given
        val payments = listOf(
            PaymentPart("cash", 10.0, null, "COMPLETED", null),
            PaymentPart("qr", 10.0, "qrdata", "SUCCESS", null)
        )

        // When
        val isValid = checkoutManager.validatePayments(payments)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `validatePayments should return false for pending payments`() {
        // Given
        val payments = listOf(
            PaymentPart("cash", 10.0, null, "COMPLETED", null),
            PaymentPart("qr", 10.0, "qrdata", "PENDING", null)
        )

        // When
        val isValid = checkoutManager.validatePayments(payments)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `processCheckout with multiple offers should create correct sale items`() = runTest {
        // Given
        val offer1 = createTestOffer(offerId = "offer1", price = 10.0)
        val offer2 = createTestOffer(offerId = "offer2", price = 15.0)
        val basketItems = listOf(
            BasketItem(offer1, 2),
            BasketItem(offer2, 1)
        )
        val payments = listOf(PaymentPart("cash", 35.0, null, "COMPLETED", null))

        coEvery { saleRepository.upsertSale(any()) } just Runs
        coEvery { inventoryTransactionRepository.recordTransaction(any()) } just Runs

        // When
        val result = checkoutManager.processCheckout(basketItems, payments)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            saleRepository.upsertSale(match { sale ->
                sale.items.size == 2 &&
                        sale.items[0].offerId == "offer1" &&
                        sale.items[0].quantity == 2 &&
                        sale.items[1].offerId == "offer2" &&
                        sale.items[1].quantity == 1
            })
        }
    }

    private fun createTestOffer(
        offerId: String = "offer1",
        price: Double = 10.0
    ) = Offer(
        offerId = offerId,
        productId = "prod1",
        title = "Test Offer",
        image = null,
        amountInInventory = 100,
        type = "UNIT",
        price = price,
        uom = "piece",
        taxPercentage = 0.0
    )
}