package com.gauravbajaj.employeesdirectory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.model.EmployeeType
import com.gauravbajaj.employeesdirectory.data.model.EmployeesResponse
import com.gauravbajaj.employeesdirectory.data.repository.EmployeesRepository
import com.gauravbajaj.employeesdirectory.data.FakeEmployeesApiService
import com.gauravbajaj.employeesdirectory.ui.base.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
/**
 * Unit tests for [EmployeeListViewModel].
 *
 * These tests verify that the view model loads the list of employees
 * correctly and updates the UI state accordingly.
 *
 * These tests use [TestCoroutineDispatcher] to control the coroutine
 * dispatcher and [InstantTaskExecutorRule] to execute the architecture
 * components' background jobs synchronously.
 */
@ExperimentalCoroutinesApi
class EmployeeListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: EmployeesRepository
    private lateinit var apiService: FakeEmployeesApiService

    private lateinit var viewModel: EmployeeListViewModel

    private val sampleEmployee = Employee(
        uuid = "test-uuid",
        full_name = "John Doe",
        phone_number = null,
        email_address = "john@example.com",
        biography = null,
        photo_url_small = null,
        photo_url_large = null,
        team = "Engineering",
        employee_type = EmployeeType.FULL_TIME
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        apiService = FakeEmployeesApiService()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should update state with employees on success`() = runTest {
        // Given
        apiService.employeesResponse = Response.success(EmployeesResponse(listOf(sampleEmployee)))
        repository = EmployeesRepository(apiService)

        // When
        viewModel = EmployeeListViewModel(repository)
        viewModel.loadEmployees()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state is UIState.Loading)
            assertTrue(state is UIState.Success)
            val successState = state as UIState.Success<List<Employee>>
            assertEquals(1, successState.data.size)
            assertEquals(sampleEmployee, successState.data.first())
        }
    }

    @Test
    fun `should update state with error on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        // Instead of using the flowOf(..), simulate via the fake API service and repository logic
        apiService.exceptionToThrow = java.io.IOException(errorMessage)
        repository = EmployeesRepository(apiService)

        // When
        viewModel = EmployeeListViewModel(repository)
        viewModel.loadEmployees()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state is UIState.Loading)
            assertTrue(state is UIState.Error)
            val errorState = state as UIState.Error
            // Depending on repo logic, if it emits a specific message, use errorState.message. Else, could be a standard error text
            assertTrue(errorState.message.contains("internet") || errorState.message.contains("error"), "Actual: ${'$'}{errorState.message}")
        }
    }
}