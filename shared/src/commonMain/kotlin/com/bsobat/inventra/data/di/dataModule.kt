package com.bsobat.inventra.data.di

import com.bsobat.inventra.basket.usecase.AddBasketItemUseCase
import com.bsobat.inventra.basket.usecase.ClearBasketUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketItemsUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketSubtotalUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTaxUseCase
import com.bsobat.inventra.basket.usecase.ObserveBasketTotalUseCase
import com.bsobat.inventra.basket.usecase.RemoveBasketItemUseCase
import com.bsobat.inventra.basket.usecase.UpdateBasketItemQuantityUseCase
import com.bsobat.inventra.category.domain.usecase.AddCategoryUseCase
import com.bsobat.inventra.category.domain.usecase.DeleteCategoryUseCase
import com.bsobat.inventra.category.domain.usecase.GetCategoriesUseCase
import com.bsobat.inventra.category.domain.usecase.UpdateCategoryUseCase
import com.bsobat.inventra.checkout.usecase.CalculateChangeUseCase
import com.bsobat.inventra.checkout.usecase.CalculateTotalUseCase
import com.bsobat.inventra.checkout.usecase.DeletePaymentMethodUseCase
import com.bsobat.inventra.checkout.usecase.EnablePaymentMethodUseCase
import com.bsobat.inventra.checkout.usecase.GetPaymentMethodUseCase
import com.bsobat.inventra.checkout.usecase.InitializeDefaultPaymentMethodsUseCase
import com.bsobat.inventra.checkout.usecase.ObservePaymentMethodsUseCase
import com.bsobat.inventra.checkout.usecase.ProcessCheckoutUseCase
import com.bsobat.inventra.checkout.usecase.SavePaymentMethodConfigUseCase
import com.bsobat.inventra.checkout.usecase.ValidatePaymentsUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyNameUseCase
import com.bsobat.inventra.config.usecase.ObserveConfigUseCase
import com.bsobat.inventra.config.usecase.ObserveEventNameUseCase
import com.bsobat.inventra.config.usecase.ObservePincodeUseCase
import com.bsobat.inventra.config.usecase.RemoveConfigUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyNameUseCase
import com.bsobat.inventra.config.usecase.UpdateConfigUseCase
import com.bsobat.inventra.config.usecase.UpdateEventNameUseCase
import com.bsobat.inventra.config.usecase.UpdatePincodeUseCase
import com.bsobat.inventra.data.repository.BasketRepository
import com.bsobat.inventra.data.repository.BasketRepositoryImpl
import com.bsobat.inventra.data.repository.CategoryRepository
import com.bsobat.inventra.data.repository.CategoryRepositoryImpl
import com.bsobat.inventra.data.repository.ConfigurationRepository
import com.bsobat.inventra.data.repository.ConfigurationRepositoryImpl
import com.bsobat.inventra.data.repository.InventoryTransactionRepository
import com.bsobat.inventra.data.repository.InventoryTransactionRepositoryImpl
import com.bsobat.inventra.data.repository.OfferRepository
import com.bsobat.inventra.data.repository.OfferRepositoryImpl
import com.bsobat.inventra.data.repository.PaymentMethodRepository
import com.bsobat.inventra.data.repository.PaymentMethodRepositoryImpl
import com.bsobat.inventra.data.repository.ProductRepository
import com.bsobat.inventra.data.repository.ProductRepositoryImpl
import com.bsobat.inventra.data.repository.SaleRepository
import com.bsobat.inventra.data.repository.SaleRepositoryImpl
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.manager.CheckoutManager
import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.PlatformSyncDependencies
import com.bsobat.inventra.manager.sync.CloudStorageProvider
import com.bsobat.inventra.manager.sync.drive.GoogleDriveAuthManager
import com.bsobat.inventra.offer.domain.usecase.AddOfferUseCase
import com.bsobat.inventra.offer.domain.usecase.DeleteOfferUseCase
import com.bsobat.inventra.offer.domain.usecase.GetOfferByIdUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveAllOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveLowStockOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.ObserveOffersByProductUseCase
import com.bsobat.inventra.offer.domain.usecase.SearchOffersUseCase
import com.bsobat.inventra.offer.domain.usecase.UpdateOfferInventoryUseCase
import com.bsobat.inventra.offer.domain.usecase.UpdateOfferUseCase
import com.bsobat.inventra.product.domain.usecase.AddProductToCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.AddProductUseCase
import com.bsobat.inventra.product.domain.usecase.DeleteProductUseCase
import com.bsobat.inventra.product.domain.usecase.DeleteProductsFromCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.GetAllProductsUseCase
import com.bsobat.inventra.product.domain.usecase.GetProductByIdUseCase
import com.bsobat.inventra.product.domain.usecase.GetProductsByCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.RemoveProductFromCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.SearchProductsUseCase
import com.bsobat.inventra.product.domain.usecase.UpdateProductUseCase
import com.bsobat.inventra.sale.usecases.DeleteAllSalesUseCase
import com.bsobat.inventra.sale.usecases.DeleteSaleUseCase
import com.bsobat.inventra.sale.usecases.GetSaleByIdUseCase
import com.bsobat.inventra.sale.usecases.ObserveSalesUseCase
import com.bsobat.inventra.sale.usecases.UpsertSaleUseCase
import com.inventra.database.InventraDatabase
import inventra.CategoryProductQueries
import inventra.CategoryQueries
import inventra.ConfigurationQueries
import inventra.OfferQueries
import inventra.ProductQueries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

expect fun provideSqlDriver(context: ContextProvider): app.cash.sqldelight.db.SqlDriver
expect fun provideAppFilesDir(context: ContextProvider): String

val dataModule = module {
    single { PlatformSyncDependencies(contextProvider = get()) }

    // Cloud provider
    single { GoogleDriveAuthManager(
        contextProvider = get(),
        dataExportImportManager = get(),
        platformDependencies = get()
    ) }

    single { provideSqlDriver(get()) }
    single { InventraDatabase(get()) }

    // Sale Use Cases
    factory { ObserveSalesUseCase(get()) }
    factory { GetSaleByIdUseCase(get()) }
    factory { UpsertSaleUseCase(get()) }
    factory { DeleteSaleUseCase(get()) }
    factory { DeleteAllSalesUseCase(get()) }

    // Queries
    single<CategoryQueries> { get<InventraDatabase>().categoryQueries }
    single<ProductQueries> { get<InventraDatabase>().productQueries }
    single<CategoryProductQueries> { get<InventraDatabase>().categoryProductQueries }
    single<OfferQueries> { get<InventraDatabase>().offerQueries }
    single<ConfigurationQueries> { get<InventraDatabase>().configurationQueries }

    // Repositories
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<OfferRepository> { OfferRepositoryImpl(get(), Dispatchers.Default) }
    single<BasketRepository> { BasketRepositoryImpl() }
    single<SaleRepository> { SaleRepositoryImpl(get()) }
    single<InventoryTransactionRepository> { InventoryTransactionRepositoryImpl(get()) }
    single<PaymentMethodRepository> { PaymentMethodRepositoryImpl(get()) }
    single<ConfigurationRepository> {
        ConfigurationRepositoryImpl(
            configurationQueries = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }

    // Managers
    single { CheckoutManager(get(), get(), get()) }
    single { DataExportImportManager(get(), provideAppFilesDir(get())) }

    // Category Use Cases
    factory { GetCategoriesUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get(), get()) }

    // Product Use Cases
    factory { GetAllProductsUseCase(get()) }
    factory { GetProductsByCategoryUseCase(get()) }
    factory { AddProductUseCase(get(), get()) }
    factory { UpdateProductUseCase(get()) }
    factory {
        DeleteProductUseCase(
            repository = get(),
            deleteOfferUseCase = get()
        )
    }
    factory {
        DeleteProductsFromCategoryUseCase(
            repository = get(),
            getProductsByCategoryUseCase = get(),
            deleteOfferUseCase = get()
        )
    }
    factory { GetProductByIdUseCase(get()) }
    factory { SearchProductsUseCase(get()) }
    factory { AddProductToCategoryUseCase(get()) }
    factory { RemoveProductFromCategoryUseCase(get()) }

    // Offer Use Cases
    factory { ObserveAllOffersUseCase(get()) }
    factory { ObserveOffersByProductUseCase(get()) }
    factory { ObserveLowStockOffersUseCase(get()) }
    factory { AddOfferUseCase(get()) }
    factory { UpdateOfferUseCase(get()) }
    factory { UpdateOfferInventoryUseCase(get()) }
    factory { DeleteOfferUseCase(get()) }
    factory { GetOfferByIdUseCase(get()) }
    factory { SearchOffersUseCase(get()) }

    // Basket Use Cases
    factory { AddBasketItemUseCase(get()) }
    factory { UpdateBasketItemQuantityUseCase(get()) }
    factory { RemoveBasketItemUseCase(get()) }
    factory { ClearBasketUseCase(get()) }
    factory { ObserveBasketItemsUseCase(get()) }
    factory { ObserveBasketSubtotalUseCase(get()) }
    factory { ObserveBasketTaxUseCase(get()) }
    factory { ObserveBasketTotalUseCase(get()) }

    // Checkout Use Cases
    factory { ProcessCheckoutUseCase(get()) }
    factory { CalculateTotalUseCase(get()) }
    factory { CalculateChangeUseCase(get()) }
    factory { ValidatePaymentsUseCase(get()) }
    factory { ObservePaymentMethodsUseCase(get()) }
    factory { GetPaymentMethodUseCase(get()) }
    factory { SavePaymentMethodConfigUseCase(get()) }
    factory { EnablePaymentMethodUseCase(get()) }
    factory { InitializeDefaultPaymentMethodsUseCase(get()) }
    factory { DeletePaymentMethodUseCase(get()) }

    // Configuration Use Cases
    factory { ObserveConfigUseCase(get()) }
    factory { UpdateConfigUseCase(get()) }
    factory { RemoveConfigUseCase(get()) }
    factory { ObserveCompanyNameUseCase(get()) }
    factory { UpdateCompanyNameUseCase(get()) }
    factory { ObserveCompanyLogoUseCase(get()) }
    factory { UpdateCompanyLogoUseCase(get()) }
    factory { ObserveCompanyDescriptionUseCase(get()) }
    factory { UpdateCompanyDescriptionUseCase(get()) }
    factory { ObserveEventNameUseCase(get()) }
    factory { UpdateEventNameUseCase(get()) }
    factory { ObservePincodeUseCase(get()) }
    factory { UpdatePincodeUseCase(get()) }

    // Other Use Cases
    single { AdminPinCheckUseCase(observePincodeUseCase = get()) }
}