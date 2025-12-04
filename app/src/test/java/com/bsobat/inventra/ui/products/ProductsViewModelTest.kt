package com.bsobat.inventra.ui.products

import app.cash.turbine.test
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.product.domain.usecase.AddProductUseCase
import com.bsobat.inventra.product.domain.usecase.DeleteProductUseCase
import com.bsobat.inventra.product.domain.usecase.GetAllProductsUseCase
import com.bsobat.inventra.product.domain.usecase.GetProductsByCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.RemoveProductFromCategoryUseCase
import com.bsobat.inventra.product.domain.usecase.SearchProductsUseCase
import com.bsobat.inventra.product.domain.usecase.UpdateProductUseCase
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
class ProductsViewModelTest {

    private lateinit var getAllProductsUseCase: GetAllProductsUseCase
    private lateinit var getProductsByCategoryUseCase: GetProductsByCategoryUseCase
    private lateinit var addProductUseCase: AddProductUseCase
    private lateinit var updateProductUseCase: UpdateProductUseCase
    private lateinit var deleteProductUseCase: DeleteProductUseCase
    private lateinit var searchProductsUseCase: SearchProductsUseCase
    private lateinit var removeProductFromCategoryUseCase: RemoveProductFromCategoryUseCase
    private lateinit var adminPinCheckUseCase: AdminPinCheckUseCase
    private lateinit var contextProvider: ContextProvider
    private lateinit var viewModel: ProductsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllProductsUseCase = mockk()
        getProductsByCategoryUseCase = mockk()
        addProductUseCase = mockk(relaxed = true)
        updateProductUseCase = mockk(relaxed = true)
        deleteProductUseCase = mockk(relaxed = true)
        searchProductsUseCase = mockk()
        removeProductFromCategoryUseCase = mockk(relaxed = true)
        adminPinCheckUseCase = mockk(relaxed = true)
        contextProvider = mockk()

        viewModel = ProductsViewModel(
            getAllProductsUseCase,
            getProductsByCategoryUseCase,
            addProductUseCase,
            updateProductUseCase,
            deleteProductUseCase,
            searchProductsUseCase,
            removeProductFromCategoryUseCase,
            adminPinCheckUseCase,
            contextProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProducts with categoryId should emit success with category products`() = runTest {
        val categoryId = "cat1"
        val products = listOf(
            Product("1", "Product B", "", null),
            Product("2", "Product A", "", null)
        )
        val screen = Screen.ProductsScreen(categoryId, "Category")

        every { getProductsByCategoryUseCase(categoryId) } returns flowOf(products)

        viewModel.uiState.test {
            viewModel.loadProducts(screen)
            assertEquals(ProductsUiState.Loading, awaitItem())
            val successState = awaitItem() as ProductsUiState.Success
            assertEquals(2, successState.products.size)
            assertEquals("Product A", successState.products[0].title)
        }
    }

    @Test
    fun `loadProducts without categoryId should emit success with all products`() = runTest {
        val products = listOf(
            Product("1", "Product B", "", null),
            Product("2", "Product A", "", null)
        )
        val screen = Screen.ProductsScreen(null, "")

        every { getAllProductsUseCase() } returns flowOf(products)

        viewModel.uiState.test {
            viewModel.loadProducts(screen)
            assertEquals(ProductsUiState.Loading, awaitItem())
            val successState = awaitItem() as ProductsUiState.Success
            assertEquals("Product A", successState.products[0].title)
        }
    }

    @Test
    fun `searchProducts should emit success with search results`() = runTest {
        val query = "test"
        val products = listOf(
            Product("1", "Test Product", "", null)
        )

        coEvery { searchProductsUseCase(query) } returns flowOf(products)

        viewModel.uiState.test {
            viewModel.searchProducts(query)
            assertEquals(ProductsUiState.Loading, awaitItem())
            val successState = awaitItem() as ProductsUiState.Success
            assertEquals(products, successState.products)
            assertEquals(query, successState.searchQuery)
        }
    }

    @Test
    fun `addProduct without categoryId should emit error`() = runTest {
        val product = Product("1", "New Product", "", null)
        val screen = Screen.ProductsScreen(null, "")

        viewModel.uiState.test {
            viewModel.addProduct(product, screen)
            assertEquals(ProductsUiState.Loading, awaitItem())
            val errorState = awaitItem() as ProductsUiState.Error
            assertEquals("Please select a category to add product", errorState.message)
        }
    }

    @Test
    fun `deleteProduct should call deleteProductUseCase and reload products`() = runTest {
        val categoryId = "cat1"
        val product = Product("1", "Product", "", null)
        val screen = Screen.ProductsScreen(categoryId, "Category")

        every { getProductsByCategoryUseCase(categoryId) } returns flowOf(emptyList())

        viewModel.deleteProduct(product, screen)

        coVerify { deleteProductUseCase("1") }
        coVerify { removeProductFromCategoryUseCase("1", categoryId) }
    }

}
