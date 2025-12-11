package com.bsobat.inventra.manager

import com.inventra.database.InventraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class ExportData(
    val categories: List<CategoryExport>,
    val products: List<ProductExport>,
    val offers: List<OfferExport>,
    val categoryProducts: List<CategoryProductExport>,
    val events: List<EventExport>,
    val sales: List<SaleExport>,
    val saleItems: List<SaleItemExport>,
    val paymentParts: List<PaymentPartExport>,
    val inventoryTransactions: List<InventoryTransactionExport>,
    val paymentMethods: List<PaymentMethodExport>,
    val exportTimestamp: String,
    val imageFiles: List<String>,
    val configurations: List<ConfigurationExport> = emptyList(),
    )

@Serializable
data class ConfigurationExport(
    val key: String,
    val value: String
)

@Serializable
data class CategoryExport(val categoryId: String, val title: String, val description: String?, val image: String?, val taxPercentage: Double)

@Serializable
data class ProductExport(val productId: String, val title: String, val description: String?, val image: String?, val skuCode: String?, val barcode: String?)

@Serializable
data class OfferExport(val offerId: String, val productId: String, val title: String, val image: String?, val amountInInventory: Long, val type: String, val price: Double, val uom: String, val taxPercentage: Double)

@Serializable
data class CategoryProductExport(val categoryId: String, val productId: String)

@Serializable
data class EventExport(val eventId: String, val title: String, val description: String?, val startDate: String, val endDate: String)

@Serializable
data class SaleExport(val saleId: String, val timestamp: String, val eventId: String?)

@Serializable
data class SaleItemExport(val saleId: String, val offerId: String, val description: String?, val quantity: Long, val unitPrice: Double, val totalPrice: Double, val taxPercentage: Double, val inventoryAdjusted: Long)

@Serializable
data class PaymentPartExport(val saleId: String, val method: String, val amount: Double, val qrCodeData: String?, val status: String, val note: String?)

@Serializable
data class InventoryTransactionExport(val transactionId: String, val offerId: String, val changeAmount: Long, val reason: String, val timestamp: String, val eventId: String?)

@Serializable
data class PaymentMethodExport(val methodId: String, val name: String, val type: String, val enabled: Long, val configData: String?)

class DataExportImportManager(
    private val database: InventraDatabase,
    private val appFilesDir: String // Path to app's files directory
) {
    private val json = Json { prettyPrint = true }
    private val fileSystem = FileSystem.SYSTEM

    suspend fun exportToZip(outputPath: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val exportData = collectAllData()
            val tempDir = createTempDirectory()

            // Write JSON data
            val dataFile = tempDir.resolve("data.json")
            fileSystem.write(dataFile) {
                writeUtf8(json.encodeToString(exportData))
            }

            // Copy image files
            val imagesDir = tempDir.resolve("images")
            fileSystem.createDirectory(imagesDir)

            exportData.imageFiles.forEach { imageName ->
                val sourceFile = appFilesDir.toPath().resolve(imageName)
                if (fileSystem.exists(sourceFile)) {
                    val destFile = imagesDir.resolve(imageName)
                    fileSystem.copy(sourceFile, destFile)
                }
            }

            // Create ZIP (using platform-specific implementation)
            createZipFromDirectory(tempDir, outputPath.toPath())

            // Cleanup temp directory
            fileSystem.deleteRecursively(tempDir)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromZip(inputPath: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val tempDir = createTempDirectory()

            // Extract ZIP (using platform-specific implementation)
            extractZipToDirectory(inputPath.toPath(), tempDir)

            // Read JSON data
            val dataFile = tempDir.resolve("data.json")
            val jsonContent = fileSystem.read(dataFile) {
                readUtf8()
            }
            val exportData = json.decodeFromString<ExportData>(jsonContent)

            // Copy images to app files directory
            val imagesDir = tempDir.resolve("images")
            if (fileSystem.exists(imagesDir)) {
                exportData.imageFiles.forEach { imageName ->
                    val sourceFile = imagesDir.resolve(imageName)
                    if (fileSystem.exists(sourceFile)) {
                        val destFile = appFilesDir.toPath().resolve(imageName)
                        fileSystem.copy(sourceFile, destFile)
                    }
                }
            }

            // Import database data
            importData(exportData)

            // Cleanup temp directory
            fileSystem.deleteRecursively(tempDir)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun collectAllData(): ExportData {
        val imageFiles = mutableSetOf<String>()

        val categories = database.categoryQueries.exportAllCategories().executeAsList().map {
            it.image?.let { img -> imageFiles.add(extractFileName(img)) }
            CategoryExport(it.categoryId, it.title, it.description, it.image, it.taxPercentage)
        }

        val products = database.productQueries.exportAllProductsWithDetails().executeAsList().map {
            it.image?.let { img -> imageFiles.add(extractFileName(img)) }
            ProductExport(it.productId, it.title, it.description, it.image, it.skuCode, it.barcode)
        }

        val offers = database.offerQueries.exportAllOffersWithDetails().executeAsList().map {
            it.image?.let { img -> imageFiles.add(extractFileName(img)) }
            OfferExport(it.offerId, it.productId, it.title, it.image, it.amountInInventory, it.type, it.price, it.uom, it.taxPercentage)
        }

        // Add configuration export
        val configurations = database.configurationQueries.allConfig().executeAsList().map {
            // Add company logo to image files if it exists
            if (it.key == "company_logo" && !it.value_.isEmpty()) {
                imageFiles.add(extractFileName(it.value_))
            }
            ConfigurationExport(it.key, it.value_)
        }

        return ExportData(
            categories = categories,
            products = products,
            offers = offers,
            categoryProducts = database.categoryProductQueries.exportAllCategoryProducts().executeAsList().map {
                CategoryProductExport(it.categoryId, it.productId)
            },
            events = database.eventQueries.exportAllEvents().executeAsList().map {
                EventExport(it.eventId, it.title, it.description, it.startDate, it.endDate)
            },
            sales = database.saleQueries.exportAllSales().executeAsList().map {
                SaleExport(it.saleId, it.timestamp, it.eventId)
            },
            saleItems = database.saleQueries.exportAllSaleItems().executeAsList().map {
                SaleItemExport(it.saleId, it.offerId, description = it.description, it.quantity, it.unitPrice, it.totalPrice, it.taxPercentage, it.inventoryAdjusted)
            },
            paymentParts = database.saleQueries.exportAllPaymentParts().executeAsList().map {
                PaymentPartExport(it.saleId, it.method, it.amount, it.qrCodeData, it.status, it.note)
            },
            inventoryTransactions = database.inventoryTransactionQueries.exportAllTransactions().executeAsList().map {
                InventoryTransactionExport(it.transactionId, it.offerId, it.changeAmount, it.reason, it.timestamp, it.eventId)
            },
            paymentMethods = database.paymentMethodQueries.exportAllPaymentMethods().executeAsList().map {
                PaymentMethodExport(it.methodId, it.name, it.type, it.enabled, it.configData)
            },
            configurations = configurations, // Add this
            exportTimestamp = Clock.System.now().toString(),
            imageFiles = imageFiles.toList()
        )
    }

    private fun extractFileName(path: String): String {
        return path.substringAfterLast('/')
    }

    @OptIn(ExperimentalTime::class)
    private fun createTempDirectory(): Path {
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("inventra_export_${Clock.System.now().nanosecondsOfSecond}")
        fileSystem.createDirectory(tempPath)
        return tempPath
    }

    private fun importData(data: ExportData) {
        database.transaction {
            database.saleQueries.deleteAllSales()
            database.inventoryTransactionQueries.deleteAll()

            data.categories.forEach {
                database.categoryQueries.insertCategory(it.categoryId, it.title, it.description, it.image, it.taxPercentage)
            }

            data.products.forEach {
                database.productQueries.insertProduct(it.productId, it.title, it.description, it.image, it.skuCode, it.barcode)
            }

            data.offers.forEach {
                database.offerQueries.insertOffer(it.offerId, it.productId, it.title, it.image, it.amountInInventory, it.type, it.price, it.uom, it.taxPercentage)
            }

            data.categoryProducts.forEach {
                database.categoryProductQueries.insertCategoryProduct(it.categoryId, it.productId)
            }

            data.events.forEach {
                database.eventQueries.insertEvent(it.eventId, it.title, it.description, it.startDate, it.endDate)
            }

            data.sales.forEach {
                database.saleQueries.insertSale(it.saleId, it.timestamp, it.eventId)
            }

            data.saleItems.forEach {
                database.saleQueries.insertSaleItem(it.saleId, it.offerId, description = it.description, it.quantity, it.unitPrice, it.totalPrice, it.taxPercentage, it.inventoryAdjusted)
            }

            data.paymentParts.forEach {
                database.saleQueries.insertPaymentPart(it.saleId, it.method, it.amount, it.qrCodeData, it.status, it.note)
            }

            data.inventoryTransactions.forEach {
                database.inventoryTransactionQueries.insertTransaction(it.transactionId, it.offerId, it.changeAmount, it.reason, it.timestamp, it.eventId)
            }

            data.paymentMethods.forEach {
                database.paymentMethodQueries.insertOrUpdate(it.methodId, it.name, it.type, it.enabled, it.configData)
            }

            // Import configurations
            data.configurations.forEach {
                database.configurationQueries.insertOrReplaceConfig(it.key, it.value)
            }
        }
    }

}

expect suspend fun DataExportImportManager.createZipFromDirectory(sourceDir: Path, zipFile: Path)
expect suspend fun DataExportImportManager.extractZipToDirectory(zipFile: Path, targetDir: Path)
