package com.bsobat.inventra.ui.config

import app.cash.turbine.test
import com.bsobat.inventra.config.usecase.ObserveCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.ObserveCompanyNameUseCase
import com.bsobat.inventra.config.usecase.ObserveEventNameUseCase
import com.bsobat.inventra.config.usecase.ObservePincodeUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyDescriptionUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyLogoUseCase
import com.bsobat.inventra.config.usecase.UpdateCompanyNameUseCase
import com.bsobat.inventra.config.usecase.UpdateEventNameUseCase
import com.bsobat.inventra.config.usecase.UpdatePincodeUseCase
import com.bsobat.inventra.data.di.ContextProvider
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
class ConfigurationViewModelTest {

    private lateinit var observeCompanyNameUseCase: ObserveCompanyNameUseCase
    private lateinit var observeCompanyLogoUseCase: ObserveCompanyLogoUseCase
    private lateinit var observePincodeUseCase: ObservePincodeUseCase
    private lateinit var observeCompanyDescriptionUseCase: ObserveCompanyDescriptionUseCase
    private lateinit var observeEventNameUseCase: ObserveEventNameUseCase
    private lateinit var updateCompanyNameUseCase: UpdateCompanyNameUseCase
    private lateinit var updatePincodeUseCase: UpdatePincodeUseCase
    private lateinit var updateCompanyLogoUseCase: UpdateCompanyLogoUseCase
    private lateinit var updateCompanyDescriptionUseCase: UpdateCompanyDescriptionUseCase
    private lateinit var updateEventNameUseCase: UpdateEventNameUseCase
    private lateinit var contextProvider: ContextProvider
    private lateinit var viewModel: ConfigurationViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        observeCompanyNameUseCase = mockk()
        observeCompanyLogoUseCase = mockk()
        observeCompanyDescriptionUseCase = mockk()
        observeEventNameUseCase = mockk()
        updateCompanyNameUseCase = mockk(relaxed = true)
        updateCompanyLogoUseCase = mockk(relaxed = true)
        updateCompanyDescriptionUseCase = mockk(relaxed = true)
        updateEventNameUseCase = mockk(relaxed = true)
        observePincodeUseCase = mockk()
        updatePincodeUseCase = mockk(relaxed = true)
        contextProvider = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should emit success with configuration data`() = runTest {
        every { observeCompanyNameUseCase() } returns flowOf("Test Company")
        every { observeCompanyLogoUseCase() } returns flowOf("logo.jpg")
        every { observeCompanyDescriptionUseCase() } returns flowOf("Test Description")
        every { observeEventNameUseCase() } returns flowOf("Test Event")
        coEvery { observePincodeUseCase() } returns flowOf(null)

        viewModel = ConfigurationViewModel(
            observeCompanyNameUseCase,
            observeCompanyLogoUseCase,
            observeCompanyDescriptionUseCase,
            observeEventNameUseCase,
            observePincodeUseCase,
            updateCompanyNameUseCase,
            updateCompanyLogoUseCase,
            updateCompanyDescriptionUseCase,
            updateEventNameUseCase,
            updatePincodeUseCase,
            contextProvider
        )

        val expectedData = ConfigurationData(
            companyName = "Test Company",
            companyLogo = "logo.jpg",
            companyDescription = "Test Description",
            eventName = "Test Event",
            pincode = null
        )

        viewModel.uiState.test {
            assertEquals(ConfigurationUiState.Success(expectedData), awaitItem())
        }
    }

    @Test
    fun `updateCompanyName should update editable data`() = runTest {
        every { observeCompanyNameUseCase() } returns flowOf("")
        every { observeCompanyLogoUseCase() } returns flowOf(null)
        every { observeCompanyDescriptionUseCase() } returns flowOf("")
        every { observeEventNameUseCase() } returns flowOf("")
        coEvery { observePincodeUseCase() } returns flowOf(null)

        viewModel = ConfigurationViewModel(
            observeCompanyNameUseCase,
            observeCompanyLogoUseCase,
            observeCompanyDescriptionUseCase,
            observeEventNameUseCase,
            observePincodeUseCase,
            updateCompanyNameUseCase,
            updateCompanyLogoUseCase,
            updateCompanyDescriptionUseCase,
            updateEventNameUseCase,
            updatePincodeUseCase,
            contextProvider
        )

        viewModel.updateCompanyName("New Name")

        assertEquals("New Name", viewModel.editableData.value.companyName)
    }

    @Test
    fun `updateCompanyDescription should update editable data`() = runTest {
        every { observeCompanyNameUseCase() } returns flowOf("")
        every { observeCompanyLogoUseCase() } returns flowOf(null)
        every { observeCompanyDescriptionUseCase() } returns flowOf("")
        every { observeEventNameUseCase() } returns flowOf("")
        coEvery { observePincodeUseCase() } returns flowOf(null)

        viewModel = ConfigurationViewModel(
            observeCompanyNameUseCase,
            observeCompanyLogoUseCase,
            observeCompanyDescriptionUseCase,
            observeEventNameUseCase,
            observePincodeUseCase,
            updateCompanyNameUseCase,
            updateCompanyLogoUseCase,
            updateCompanyDescriptionUseCase,
            updateEventNameUseCase,
            updatePincodeUseCase,
            contextProvider
        )

        viewModel.updateCompanyDescription("New Description")

        assertEquals("New Description", viewModel.editableData.value.companyDescription)
    }

    @Test
    fun `updateEventName should update editable data`() = runTest {
        every { observeCompanyNameUseCase() } returns flowOf("")
        every { observeCompanyLogoUseCase() } returns flowOf(null)
        every { observeCompanyDescriptionUseCase() } returns flowOf("")
        every { observeEventNameUseCase() } returns flowOf("")
        coEvery { observePincodeUseCase() } returns flowOf(null)

        viewModel = ConfigurationViewModel(
            observeCompanyNameUseCase,
            observeCompanyLogoUseCase,
            observeCompanyDescriptionUseCase,
            observeEventNameUseCase,
            observePincodeUseCase,
            updateCompanyNameUseCase,
            updateCompanyLogoUseCase,
            updateCompanyDescriptionUseCase,
            updateEventNameUseCase,
            updatePincodeUseCase,
            contextProvider
        )

        viewModel.updateEventName("New Event")

        assertEquals("New Event", viewModel.editableData.value.eventName)
    }

    @Test
    fun `saveConfiguration should call all update use cases`() = runTest {
        every { observeCompanyNameUseCase() } returns flowOf("Company")
        every { observeCompanyLogoUseCase() } returns flowOf("logo.jpg")
        every { observeCompanyDescriptionUseCase() } returns flowOf("Description")
        every { observeEventNameUseCase() } returns flowOf("Event")
        coEvery { observePincodeUseCase() } returns flowOf(null)

        viewModel = ConfigurationViewModel(
            observeCompanyNameUseCase,
            observeCompanyLogoUseCase,
            observeCompanyDescriptionUseCase,
            observeEventNameUseCase,
            observePincodeUseCase,
            updateCompanyNameUseCase,
            updateCompanyLogoUseCase,
            updateCompanyDescriptionUseCase,
            updateEventNameUseCase,
            updatePincodeUseCase,
            contextProvider
        )

        viewModel.uiState.test {
            awaitItem()
        }
        viewModel.saveConfiguration()

        coVerify { updateCompanyNameUseCase("Company") }
        coVerify { updateCompanyLogoUseCase("logo.jpg") }
        coVerify { updateCompanyDescriptionUseCase("Description") }
        coVerify { updateEventNameUseCase("Event") }
    }
}
