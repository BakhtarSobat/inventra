package com.bsobat.inventra.ui.offer

import app.cash.turbine.test
import com.bsobat.inventra.basket.usecase.AddBasketItemUseCase
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.domain.usecase.copyImageToInternalStorageUseCase
import com.bsobat.inventra.offer.domain.usecase.AddOfferUseCase
import com.bsobat.inventra.offer.domain.usecase.DeleteOfferUseCase
import com.bsobat.inventra.offer.domain.usecase.GetOfferByIdUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveAllOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveLowStockOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveOffersByProductUseCase
import com.bsobat.inventra.offer.domain.usecase.SearchOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.UpdateOfferInventoryUseCase
import com.bsobat.inventra.offer.domain.usecase.UpdateOfferUseCase
import com.bsobat.inventra.ui.navigation.Screen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OfferViewModelTest {

    private lateinit var observeAllOffersUseCase: ObserveAllOffersUseCase
    private lateinit var observeOffersByProductUseCase: ObserveOffersByProductUseCase
    private lateinit var observeLowStockOffersUseCase: ObserveLowStockOffersUseCase
    private lateinit var addOfferUseCase: AddOfferUseCase
    private lateinit var updateOfferUseCase: UpdateOfferUseCase
    private lateinit var updateOfferInventoryUseCase: UpdateOfferInventoryUseCase
    private lateinit var deleteOfferUseCase: DeleteOfferUseCase
    private lateinit var getOfferByIdUseCase: GetOfferByIdUseCase
    private lateinit var searchOffersUseCase: SearchOffersUseCase
    private lateinit var adminPinCheckUseCase: AdminPinCheckUseCase
    private lateinit var basketItemUseCase: AddBasketItemUseCase
    private lateinit var contextProvider: ContextProvider
    private lateinit var viewModel: OfferViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        observeAllOffersUseCase = mockk()
        observeOffersByProductUseCase = mockk()
        observeLowStockOffersUseCase = mockk()
        addOfferUseCase = mockk(relaxed = true)
        updateOfferUseCase = mockk(relaxed = true)
        updateOfferInventoryUseCase = mockk(relaxed = true)
        deleteOfferUseCase = mockk(relaxed = true)
        getOfferByIdUseCase = mockk()
        searchOffersUseCase = mockk()
        adminPinCheckUseCase = mockk(relaxed = true)
        basketItemUseCase = mockk(relaxed = true)
        contextProvider = mockk()

        viewModel = OfferViewModel(
            observeAllOffersUseCase,
            observeOffersByProductUseCase,
            observeLowStockOffersUseCase,
            addOfferUseCase,
            updateOfferUseCase,
            updateOfferInventoryUseCase,
            deleteOfferUseCase,
            getOfferByIdUseCase,
            searchOffersUseCase,
            adminPinCheckUseCase,
            basketItemUseCase,
            contextProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadOffersByProduct should emit success with offers`() = runTest {
        val productId = "product1"
        val offers = listOf(
            Offer("1", productId, "Offer B", "", 10, price = 10.0, uom = "kg"),
            Offer("2", productId, "Offer A", "", 20, price = 15.0, uom = "kg")
        )
        val screen = Screen.OffersScreen(productId, "Product", null)

        every { observeOffersByProductUseCase(productId) } returns flowOf(offers)

        viewModel.uiState.test {
            assertEquals(OffersUiState.Loading, awaitItem())
            viewModel.loadOffersByProduct(screen)
            val successState = awaitItem() as OffersUiState.Success
            assertEquals(2, successState.offers.size)
            assertEquals("Offer A", successState.offers[0].title)
        }
    }

    @Test
    fun `searchOffers should emit success with search results`() = runTest {
        val query = "test"
        val offers = listOf(
            Offer("1", "p1", "Test Offer", "", 10, price = 10.0, uom = "kg")
        )

        coEvery { searchOffersUseCase(query) } returns offers

        viewModel.uiState.test {
            viewModel.searchOffers(query)
            assertEquals(OffersUiState.Loading, awaitItem())
            val successState = awaitItem() as OffersUiState.Success
            assertEquals(offers, successState.offers)
            assertEquals(query, successState.searchQuery)
        }
    }

    @Test
    fun `loadLowStockOffers should emit success with low stock offers`() = runTest {
        val threshold = 10
        val offers = listOf(
            Offer("1", "p1", "Low Stock", "", 10, price = 10.0, uom = "kg")
        )

        every { observeLowStockOffersUseCase(threshold) } returns flowOf(offers)

        viewModel.uiState.test {
            viewModel.loadLowStockOffers(threshold)
            assertEquals(OffersUiState.Loading, awaitItem())
            assertEquals(OffersUiState.Success(offers), awaitItem())
        }
    }

    @Test
    fun `getOfferById should update selectedOffer`() = runTest {
        val offerId = "offer1"
        val offer = Offer(offerId, "p1", "Test", "", 10, price = 10.0, uom = "kg")

        coEvery { getOfferByIdUseCase(offerId) } returns offer

        viewModel.selectedOffer.test {
            assertEquals(null, awaitItem())
            viewModel.getOfferById(offerId)
            assertEquals(offer, awaitItem())
        }
    }

    @Test
    fun `updateInventory should call updateOfferInventoryUseCase`() = runTest {
        val offerId = "offer1"
        val newAmount = 20

        viewModel.updateInventory(offerId, newAmount)

        coVerify { updateOfferInventoryUseCase(offerId, newAmount) }
    }

    @Test
    fun `addToBasket should call basketItemUseCase`() = runTest {
        val offer = Offer("1", "p1", "Test", "", 10, price = 10.0, uom = "kg")
        val quantity = 2

        viewModel.addToBasket(offer, quantity)

        coVerify { basketItemUseCase(offer, quantity) }
    }

    @Test
    fun `clearSelectedOffer should set selectedOffer to null`() = runTest {
        val offer = Offer("1", "p1", "Test", "", 10, price = 10.0, uom = "kg")
        coEvery { getOfferByIdUseCase("1") } returns offer

        viewModel.selectedOffer.test {
            assertEquals(null, awaitItem())
            viewModel.getOfferById("1")
            assertEquals(offer, awaitItem())
            viewModel.clearSelectedOffer()
            assertEquals(null, awaitItem())
        }
    }
}
