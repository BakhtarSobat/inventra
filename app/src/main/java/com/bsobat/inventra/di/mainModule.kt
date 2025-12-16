package com.bsobat.inventra.di

import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.ui.MainActivityViewModel
import com.bsobat.inventra.ui.basket.BasketViewModel
import com.bsobat.inventra.ui.categories.CategoryViewModel
import com.bsobat.inventra.ui.checkout.CheckoutViewModel
import com.bsobat.inventra.ui.config.ConfigurationViewModel
import com.bsobat.inventra.ui.offer.OfferViewModel
import com.bsobat.inventra.ui.products.ProductsViewModel
import com.bsobat.inventra.ui.receipt.PdfExporter
import com.bsobat.inventra.ui.receipt.ReceiptViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel{
        ConfigurationViewModel(
            observeCompanyNameUseCase = get(),
            updateCompanyNameUseCase = get(),
            observeCompanyLogoUseCase = get(),
            updateCompanyLogoUseCase = get(),
            observeCompanyDescriptionUseCase = get(),
            updateCompanyDescriptionUseCase = get(),
            observeEventNameUseCase = get(),
            updateEventNameUseCase = get(),
            observePincodeUseCase = get(),
            updatePincodeUseCase = get(),
            contextProvider = get()
        )
    }

    viewModel{
        MainActivityViewModel(
            dataExportImportManager = get(),
            observeBasketItemsUseCase = get(),
            adminPinCheckUseCase = get(),
            authManager = get()
        )
    }

    viewModel{
        ReceiptViewModel(
            getSaleByIdUseCase = get(),
            pdfExporter = get(),
            observeCompanyNameUseCase = get(),
            observeCompanyLogoUseCase = get(),
            observeCompanyDescriptionUseCase = get(),
            observeEventNameUseCase = get(),
        )
    }

    viewModel{
        CheckoutViewModel(
            observeBasketItemsUseCase = get(),
            observeBasketSubtotalUseCase = get(),
            observeBasketTaxUseCase = get(),
            observeBasketTotalUseCase = get(),
            observePaymentMethodsUseCase = get(),
            processCheckoutUseCase = get(),
            savePaymentMethodConfigUseCase = get(),
            adminPinCheckUseCase = get(),
            clearBasketUseCase = get(),
            deletePaymentMethodUseCase = get()
        )
    }
    viewModel {
        OfferViewModel(
            observeAllOffersUseCase = get(),
            observeOffersByProductUseCase = get(),
            observeLowStockOffersUseCase = get(),
            addOfferUseCase = get(),
            updateOfferUseCase = get(),
            updateOfferInventoryUseCase = get(),
            deleteOfferUseCase = get(),
            getOfferByIdUseCase = get(),
            searchOffersUseCase = get(),
            adminPinCheckUseCase = get(),
            basketItemUseCase = get(),
            contextProvider = get()
        )
    }

    viewModel {
        BasketViewModel(
            observeItems = get(),
            updateQuantity = get(),
            removeItem = get(),
            observeSubtotal = get(),
            observeTax = get(),
            observeTotal = get()
        )
    }

    viewModel {
        CategoryViewModel(
            getCategories = get(),
            addCategory = get(),
            updateCategory = get(),
            deleteCategory = get(),
            contextProvider = get(),
            adminPinCheckUseCase = get()
        )
    }
    viewModel {
        ProductsViewModel(
            getAllProductsUseCase = get(),
            getProductsByCategoryUseCase = get(),
            addProductUseCase = get(),
            updateProductUseCase = get(),
            deleteProductUseCase = get(),
            searchProductsUseCase = get(),
            removeProductFromCategoryUseCase = get(),
            adminPinCheckUseCase = get(),
            contextProvider = get()
        )
    }

    factory { PdfExporter(androidContext()) }
    single<ContextProvider> {
        object : ContextProvider {
            override fun invoke(): Any = androidContext()
        }
    }
}