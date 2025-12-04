package com.bsobat.inventra.ui.categories

import app.cash.turbine.test
import com.bsobat.inventra.category.domain.usecase.AddCategoryUseCase
import com.bsobat.inventra.category.domain.usecase.DeleteCategoryUseCase
import com.bsobat.inventra.category.domain.usecase.GetCategoriesUseCase
import com.bsobat.inventra.category.domain.usecase.UpdateCategoryUseCase
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.model.Category
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.domain.usecase.copyImageToInternalStorageUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModelTest {

    private lateinit var getCategories: GetCategoriesUseCase
    private lateinit var addCategory: AddCategoryUseCase
    private lateinit var updateCategory: UpdateCategoryUseCase
    private lateinit var deleteCategory: DeleteCategoryUseCase
    private lateinit var contextProvider: ContextProvider
    private lateinit var adminPinCheckUseCase: AdminPinCheckUseCase
    private lateinit var viewModel: CategoryViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getCategories = mockk()
        addCategory = mockk(relaxed = true)
        updateCategory = mockk(relaxed = true)
        deleteCategory = mockk(relaxed = true)
        contextProvider = mockk(relaxed = true)
        adminPinCheckUseCase = mockk(relaxed = true)

        every { getCategories() } returns flowOf(emptyList())
        every { adminPinCheckUseCase.adminLoggedIn } returns MutableStateFlow(false)

        mockkStatic("com.bsobat.inventra.domain.usecase.CopyImageToInternalStorageUseCaseKt")
        coEvery { copyImageToInternalStorageUseCase(any(), any(), any()) } returns null
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `uiState should emit Loading then Success with categories on init`() = runTest {
        // Given
        val categories = listOf(
            Category("1", "Category 1", null, null, 10.0),
            Category("2", "Category 2", null, null, 5.0)
        )
        every { getCategories() } returns flowOf(categories)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val successState = awaitItem() as CategoryUiState.Success
            assertEquals(categories, successState.categories)
        }
    }

    @Test
    fun `uiState should emit Loading then Success with empty list when no categories`() = runTest {
        // Given
        every { getCategories() } returns flowOf(emptyList())

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val successState = awaitItem() as CategoryUiState.Success
            assertEquals(emptyList(), successState.categories)
        }
    }

    @Test
    fun `createCategory should call addCategory with created category`() = runTest {
        // Given
        viewModel = createViewModel()
        val title = "New Category"
        val description = "Description"
        val image = "/path/to/image.jpg"
        val taxPercentage = 15.0

        coEvery { addCategory(any()) } just Runs

        // When
        viewModel.createCategory(title, description, image, taxPercentage)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            addCategory(match {
                it.title == title &&
                it.description == description &&
                it.taxPercentage == taxPercentage
            })
        }
    }

    @Test
    fun `createCategory should handle null description and image`() = runTest {
        // Given
        viewModel = createViewModel()
        val title = "New Category"
        val taxPercentage = 10.0

        coEvery { addCategory(any()) } just Runs

        // When
        viewModel.createCategory(title, null, null, taxPercentage)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            addCategory(match {
                it.title == title &&
                it.description == null &&
                it.image == null &&
                it.taxPercentage == taxPercentage
            })
        }
    }


    @Test
    fun `editCategory should call updateCategory with updated category`() = runTest {
        // Given
        viewModel = createViewModel()
        val category = Category("1", "Updated", "Description", "/image.jpg", 12.0)

        coEvery { updateCategory(any()) } just Runs

        // When
        viewModel.editCategory(category)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { updateCategory(category) }
    }

    @Test
    fun `editCategory should copy image to internal storage`() = runTest {
        // Given
        viewModel = createViewModel()
        val category = Category("1", "Title", null, "/external/image.jpg", 10.0)
        val internalPath = "/internal/category_1.jpg"

        coEvery { copyImageToInternalStorageUseCase(any(), any(), any()) } returns internalPath
        coEvery { updateCategory(any()) } just Runs

        // When
        viewModel.editCategory(category)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            updateCategory(match { it.image == internalPath })
        }
    }

    @Test
    fun `deleteCategory should call deleteCategory with categoryId`() = runTest {
        // Given
        viewModel = createViewModel()
        val category = Category("1", "Title", null, null, 10.0)

        coEvery { deleteCategory(any()) } just Runs

        // When
        viewModel.deleteCategory(category)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { deleteCategory("1") }
    }

    private fun createViewModel() = CategoryViewModel(
        getCategories,
        addCategory,
        updateCategory,
        deleteCategory,
        contextProvider,
        adminPinCheckUseCase
    )
}
