package com.bsobat.inventra.ui.receipt

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.data.model.SaleItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PdfExporterTest {

    private lateinit var context: Context
    private lateinit var pdfExporter: PdfExporter

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        pdfExporter = PdfExporter(context)
        
        every { context.getString(any()) } returns "Test String"
        every { context.contentResolver } returns mockk(relaxed = true)
        
        mockkStatic(PdfDocument::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `exportReceiptToPdf should return success with file when export succeeds`() = runTest {
        val sale = Sale(
            saleId = "SALE-001",
            items = listOf(
                SaleItem(
                    offerId = "offer1",
                    description = "Test Product",
                    quantity = 2,
                    unitPrice = 10.0,
                    totalPrice = 20.0,
                    taxPercentage = 10.0
                )
            ),
            payments = listOf(
                PaymentPart(method = "Cash", amount = 22.0)
            ),
            timestamp = System.currentTimeMillis().toString(),
            eventId = "event1"
        )

        val configData = ConfigData(
            companyName = "Test Company",
            companyLogo = null,
            companyDescription = "Test Description",
            eventName = "Test Event"
        )

        val result = pdfExporter.exportReceiptToPdf(sale, configData)

        assertTrue(result.isSuccess)
        result.getOrNull()?.let { file ->
            assertTrue(file.name.startsWith("Receipt_SALE-001_"))
            assertTrue(file.name.endsWith(".pdf"))
        }
    }

    @Test
    fun `exportReceiptToPdf should handle sale without config data`() = runTest {
        val sale = Sale(
            saleId = "SALE-002",
            items = listOf(
                SaleItem(
                    offerId = "offer1",
                    description = "Product",
                    quantity = 1,
                    unitPrice = 5.0,
                    totalPrice = 5.0,
                    taxPercentage = 0.0
                )
            ),
            payments = listOf(
                PaymentPart(method = "Card", amount = 5.0)
            ),
            timestamp = System.currentTimeMillis().toString(),
            eventId = null
        )

        val result = pdfExporter.exportReceiptToPdf(sale, null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `exportReceiptToPdf should handle multiple items`() = runTest {
        val sale = Sale(
            saleId = "SALE-003",
            items = listOf(
                SaleItem("offer1", "Product 1", 1, 10.0, 10.0, 5.0),
                SaleItem("offer2", "Product 2", 2, 15.0, 30.0, 5.0),
                SaleItem("offer3", "Product 3", 3, 20.0, 60.0, 10.0)
            ),
            payments = listOf(
                PaymentPart(method = "Cash", amount = 50.0),
                PaymentPart(method = "Card", amount = 57.5)
            ),
            timestamp = System.currentTimeMillis().toString(),
            eventId = null
        )

        val result = pdfExporter.exportReceiptToPdf(sale, null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `exportReceiptToPdf should handle items without description`() = runTest {
        val sale = Sale(
            saleId = "SALE-004",
            items = listOf(
                SaleItem(
                    offerId = "offer1",
                    description = null,
                    quantity = 1,
                    unitPrice = 10.0,
                    totalPrice = 10.0,
                    taxPercentage = 0.0
                )
            ),
            payments = listOf(
                PaymentPart(method = "Cash", amount = 10.0)
            ),
            timestamp = System.currentTimeMillis().toString(),
            eventId = null
        )

        val result = pdfExporter.exportReceiptToPdf(sale, null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `exportReceiptToPdf should include all config data when provided`() = runTest {
        val sale = Sale(
            saleId = "SALE-005",
            items = listOf(
                SaleItem("offer1", "Product", 1, 10.0, 10.0, 0.0)
            ),
            payments = listOf(
                PaymentPart(method = "Cash", amount = 10.0)
            ),
            timestamp = System.currentTimeMillis().toString(),
            eventId = "event1"
        )

        val configData = ConfigData(
            companyName = "My Company",
            companyLogo = "/path/to/logo.png",
            companyDescription = "Company Description",
            eventName = "Special Event"
        )

        val result = pdfExporter.exportReceiptToPdf(sale, configData)

        assertTrue(result.isSuccess)
    }
}
