package com.bsobat.inventra.ui.receipt

import app.cash.turbine.test
import com.bsobat.inventra.config.usecase.ObserveCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyNameUseCase
import com.bsobat.inventra.config.usecase.ObserveEventNameUseCase
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.data.model.SaleItem
import com.bsobat.inventra.sale.usecases.GetSaleByIdUseCase
import io.mockk.coEvery
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
import java.io.File
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReceiptViewModelTest {

    private lateinit var getSaleByIdUseCase: GetSaleByIdUseCase
    private lateinit var pdfExporter: PdfExporter
    private lateinit var observeCompanyNameUseCase: ObserveCompanyNameUseCase
    private lateinit var observeCompanyLogoUseCase: ObserveCompanyLogoUseCase
    private lateinit var observeCompanyDescriptionUseCase: ObserveCompanyDescriptionUseCase
    private lateinit var observeEventNameUseCase: ObserveEventNameUseCase
    private lateinit var viewModel: ReceiptViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getSaleByIdUseCase = mockk()
        pdfExporter = mockk()
        observeCompanyNameUseCase = mockk()
        observeCompanyLogoUseCase = mockk()
        observeCompanyDescriptionUseCase = mockk()
        observeEventNameUseCase = mockk()

        every { observeCompanyNameUseCase() } returns flowOf("Test Company")
        every { observeCompanyLogoUseCase() } returns flowOf("logo.jpg")
        every { observeCompanyDescriptionUseCase() } returns flowOf("Test Description")
        every { observeEventNameUseCase() } returns flowOf("Test Event")

        viewModel = ReceiptViewModel(
            getSaleByIdUseCase,
            pdfExporter,
            observeCompanyNameUseCase,
            observeCompanyLogoUseCase,
            observeCompanyDescriptionUseCase,
            observeEventNameUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `configData should emit configuration data`() = runTest {
        val expectedConfig = ConfigData(
            companyName = "Test Company",
            companyLogo = "logo.jpg",
            companyDescription = "Test Description",
            eventName = "Test Event"
        )

        viewModel.configData.test {
            assertEquals(expectedConfig, awaitItem())
        }
    }

    @Test
    fun `loadSale should emit success when sale exists`() = runTest {
        val saleId = "sale1"
        val sale = Sale(
            saleId = saleId,
            items = listOf(SaleItem("item1", "Product", 1, 10.0, totalPrice = 10.0, taxPercentage = 0.0, inventoryAdjusted = false)),
            eventId = null,
            timestamp = "",
            payments = emptyList()
        )

        coEvery { getSaleByIdUseCase(saleId) } returns sale

        viewModel.uiState.test {
            assertEquals(ReceiptUiState.Loading, awaitItem())
            viewModel.loadSale(saleId)
            assertEquals(ReceiptUiState.Success(sale), awaitItem())
        }
    }

    @Test
    fun `loadSale should emit error when sale not found`() = runTest {
        val saleId = "nonexistent"

        coEvery { getSaleByIdUseCase(saleId) } returns null

        viewModel.uiState.test {
            assertEquals(ReceiptUiState.Loading, awaitItem())
            viewModel.loadSale(saleId)
            assertEquals(ReceiptUiState.Error("Sale not found"), awaitItem())
        }
    }

    @Test
    fun `exportToPdf should emit success when export succeeds`() = runTest {
        val saleId = "sale1"
        val sale = Sale(
            saleId = saleId,
            items = listOf(SaleItem("item1", "Product", 1, 10.0, totalPrice = 10.0, taxPercentage = 0.0, inventoryAdjusted = false)),
            eventId = null,
            timestamp = "",
            payments = emptyList()
        )
        val pdfFile = mockk<File> {
            every { absolutePath } returns "/path/to/receipt.pdf"
        }

        coEvery { getSaleByIdUseCase(saleId) } returns sale
        coEvery { pdfExporter.exportReceiptToPdf(sale, any()) } returns Result.success(pdfFile)

        viewModel.loadSale(saleId)

        viewModel.pdfExportState.test {
            assertEquals(PdfExportState.Idle, awaitItem())
            viewModel.exportToPdf()
            assertEquals(PdfExportState.Success("/path/to/receipt.pdf"), awaitItem())
        }
    }

    @Test
    fun `exportToPdf should emit error when export fails`() = runTest {
        val saleId = "sale1"
        val sale = Sale(
            saleId = saleId,
            items = listOf(SaleItem("item1", "Product", 1, 10.0, totalPrice = 10.0, taxPercentage = 0.0, inventoryAdjusted = false)),
            eventId = null,
            timestamp = "",
            payments = emptyList()
        )
        val errorMessage = "Export failed"

        coEvery { getSaleByIdUseCase(saleId) } returns sale
        coEvery { pdfExporter.exportReceiptToPdf(sale, any()) } returns Result.failure(Exception(errorMessage))

        viewModel.loadSale(saleId)

        viewModel.pdfExportState.test {
            assertEquals(PdfExportState.Idle, awaitItem())
            viewModel.exportToPdf()
            assertEquals(PdfExportState.Error(errorMessage), awaitItem())
        }
    }
}
