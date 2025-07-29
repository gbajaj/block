package com.gauravbajaj.employeesdirectory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.gauravbajaj.employeesdirectory.data.ApiException
import com.gauravbajaj.employeesdirectory.data.FakeEmployeesApiService
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.model.EmployeeType
import com.gauravbajaj.employeesdirectory.data.model.EmployeesResponse
import com.gauravbajaj.employeesdirectory.data.network.NetworkConnectivityManager
import com.gauravbajaj.employeesdirectory.data.network.NetworkStatus
import com.gauravbajaj.employeesdirectory.data.repository.EmployeesRepository
import com.gauravbajaj.employeesdirectory.ui.base.UIState
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EmployeeListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Real repository with fake API service
    private lateinit var fakeApiService: FakeEmployeesApiService
    private lateinit var realRepository: EmployeesRepository

    // Mock only the network connectivity manager
    @Mock
    private lateinit var mockNetworkManager: NetworkConnectivityManager

    private lateinit var viewModel: EmployeeListViewModel

    // Test data
    private val sampleEmployee = Employee(
        uuid = "550e8400-e29b-41d4-a716-446655440000",
        full_name = "John Doe",
        phone_number = "1234567890",
        email_address = "john.doe@example.com",
        biography = "Software engineer with 5 years of experience",
        photo_url_small = "https://example.com/photos/john_small.jpg",
        photo_url_large = "https://example.com/photos/john_large.jpg",
        team = "Engineering",
        employee_type = EmployeeType.FULL_TIME
    )

    private val sampleEmployeeList = listOf(
        sampleEmployee,
        Employee(
            uuid = "550e8400-e29b-41d4-a716-446655440001",
            full_name = "Jane Smith",
            phone_number = null,
            email_address = "jane.smith@example.com",
            biography = null,
            photo_url_small = null,
            photo_url_large = null,
            team = "Marketing",
            employee_type = EmployeeType.PART_TIME
        )
    )

    private val malformedEmployee = Employee(
        uuid = "", // Invalid - empty UUID
        full_name = "Invalid Employee",
        phone_number = null,
        email_address = "invalid@example.com",
        biography = null,
        photo_url_small = null,
        photo_url_large = null,
        team = "Invalid",
        employee_type = EmployeeType.FULL_TIME
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup fake API service and real repository
        fakeApiService = FakeEmployeesApiService()
        realRepository = EmployeesRepository(fakeApiService, mockNetworkManager)

        // Default network behavior
        whenever(mockNetworkManager.isNetworkAvailable).thenReturn(true)
        whenever(mockNetworkManager.networkStatus).thenReturn(flowOf(NetworkStatus.Available))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== INITIALIZATION TESTS =====

    @Test
    fun `initial state should be Initial`() = runTest {
        // Given
        setupSuccessfulResponse()

        // When
        viewModel = EmployeeListViewModel(realRepository)

        // Then
        assertTrue(viewModel.uiState.value is UIState.Initial)
        assertEquals(NetworkStatus.Available, viewModel.networkStatus.value)
    }

    // ===== SUCCESSFUL LOADING TESTS =====

    @Test
    fun `initiateEmployeeLoading should update state to success when API call succeeds`() =
        runTest {
            // Given
            setupSuccessfulResponse()

            // When
            viewModel = EmployeeListViewModel(realRepository)
            viewModel.initiateEmployeeLoading()
            advanceUntilIdle()

            // Then
            val finalState = viewModel.uiState.value
            assertTrue(finalState is UIState.Success)
            assertEquals(2, finalState.data.size)
            assertEquals(sampleEmployee, finalState.data.first())
        }

    @Test
    fun `loadEmployees should emit loading then success states`() = runTest {
        // Given
        setupSuccessfulResponse()
        viewModel = EmployeeListViewModel(realRepository)

        // When & Then
        viewModel.uiState.test {
            assertEquals(UIState.Initial, awaitItem())

            viewModel.initiateEmployeeLoading()

            // Should emit Loading then Success
            assertEquals(UIState.Loading, awaitItem())

            val successState = awaitItem()
            assertTrue(successState is UIState.Success)
            assertEquals(2, successState.data.size)
        }
    }

    @Test
    fun `should handle empty employee list successfully`() = runTest {
        // Given
        fakeApiService.employeesResponse = Response.success(EmployeesResponse(emptyList()))

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertTrue(finalState.data.isEmpty())
    }

    // ===== NETWORK CONNECTIVITY TESTS =====

    @Test
    fun `should emit NoNetworkException when network is unavailable`() = runTest {
        // Given
        whenever(mockNetworkManager.isNetworkAvailable).thenReturn(false)
        whenever(mockNetworkManager.networkStatus).thenReturn(flowOf(NetworkStatus.Unavailable))

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.NoNetworkException)
        assertEquals(
            "No internet connection. Please check your network and try again.",
            finalState.message
        )
    }

    @Test
    fun `should auto-retry when network becomes available after NoNetworkException`() = runTest {
        // Given
        val networkStatusFlow = MutableSharedFlow<NetworkStatus>()
        whenever(mockNetworkManager.networkStatus).thenReturn(networkStatusFlow)

        // Initially no network
        whenever(mockNetworkManager.isNetworkAvailable).thenReturn(false)

        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Verify initial no network error
        assertTrue(viewModel.uiState.value is UIState.Error)
        assertTrue((viewModel.uiState.value as UIState.Error).exception is ApiException.NoNetworkException)

        // When network becomes available
        whenever(mockNetworkManager.isNetworkAvailable).thenReturn(true)
        setupSuccessfulResponse()

        networkStatusFlow.emit(NetworkStatus.Available)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertEquals(2, finalState.data.size)
    }

    @Test
    fun `should observe network status changes`() = runTest {
        // Given
        val networkStatusFlow = MutableSharedFlow<NetworkStatus>()
        setupSuccessfulResponse()
        whenever(mockNetworkManager.networkStatus).thenReturn(networkStatusFlow)

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()

        //Verify
        networkStatusFlow.emit(NetworkStatus.Unavailable)
        advanceUntilIdle()
        assertEquals(viewModel.networkStatus.value, NetworkStatus.Available)
        //Then
        networkStatusFlow.emit(NetworkStatus.Available)
        advanceUntilIdle()
        //Verify
        assertEquals(viewModel.networkStatus.value, NetworkStatus.Available)
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `should handle UnknownHostException as NetworkException`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = UnknownHostException("Unable to resolve host")

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.NetworkException)
        assertEquals("Please check your internet connection and try again", finalState.message)
        assertTrue(finalState.isRetryable)
    }

    @Test
    fun `should handle ConnectException as NetworkException`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = ConnectException("Connection refused")

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.NetworkException)
        assertTrue(finalState.isRetryable)
    }

    @Test
    fun `should handle SocketTimeoutException as NetworkException`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = SocketTimeoutException("timeout")

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.NetworkException)
        assertTrue(finalState.isRetryable)
    }

    @Test
    fun `should handle IOException as NetworkException`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = IOException("Network error")

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.NetworkException)
        assertTrue(finalState.isRetryable)
    }

    @Test
    fun `should handle HttpException as ServerException`() = runTest {
        // Given
        val errorBody = "Not Found".toResponseBody("text/plain".toMediaType())
        val httpException = HttpException(Response.error<Any>(404, errorBody))
        fakeApiService.exceptionToThrow = httpException

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.ServerException)
        assertEquals(404, (finalState.exception as ApiException.ServerException).code)
        assertEquals("Employee data not found", finalState.message) // 404 specific message
    }

    @Test
    fun `should handle 500 server errors as retryable`() = runTest {
        // Given
        val errorBody = "Internal Server Error".toResponseBody("text/plain".toMediaType())
        val httpException = HttpException(Response.error<Any>(500, errorBody))
        fakeApiService.exceptionToThrow = httpException

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.ServerException)
        assertEquals(500, (finalState.exception as ApiException.ServerException).code)
        assertEquals(
            "Server is temporarily unavailable. Please try again later",
            finalState.message
        )
        assertTrue(finalState.isRetryable) // 5xx errors are retryable
    }

    @Test
    fun `should handle HTTP error responses from API`() = runTest {
        // Given
        val errorBody = "Bad Request".toResponseBody("text/plain".toMediaType())
        fakeApiService.employeesResponse = Response.error(400, errorBody)

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.ServerException)
        assertEquals(400, (finalState.exception as ApiException.ServerException).code)
    }

    @Test
    fun `should handle JsonDataException as ParseException`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = JsonDataException("Invalid JSON")

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.ParseException)
        assertEquals("Unable to load employee data. Please try again", finalState.message)
        assertTrue(!finalState.isRetryable) // Parse errors are not retryable
    }

    @Test
    fun `should handle RuntimeException as UnknownException`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = RuntimeException("Unexpected error")

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.UnknownException)
        assertEquals("An unexpected error occurred. Please try again", finalState.message)
        assertTrue(finalState.isRetryable)
    }

    // ===== DATA VALIDATION TESTS =====

    @Test
    fun `should reject malformed employee data`() = runTest {
        // Given - response with invalid employee (empty UUID)
        val malformedEmployees = listOf(sampleEmployee, malformedEmployee)
        fakeApiService.employeesResponse = Response.success(EmployeesResponse(malformedEmployees))

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.ParseException)
        assertTrue(finalState.exception.message!!.contains("malformed"))
        assertTrue(!finalState.isRetryable)
    }

    @Test
    fun `should handle null response body gracefully`() = runTest {
        // Given
        fakeApiService.employeesResponse = Response.success(null)

        // When
        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertTrue(finalState.data.isEmpty())
    }

    // ===== MANUAL RETRY TESTS =====

    @Test
    fun `retry should work for retryable errors`() = runTest {
        // Given - initially fail
        fakeApiService.exceptionToThrow = UnknownHostException("No network")

        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Verify initial error
        assertTrue(viewModel.uiState.value is UIState.Error)

        // When - fix the issue and retry
        fakeApiService.exceptionToThrow = null
        setupSuccessfulResponse()

        viewModel.retry()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
        assertEquals(2, finalState.data.size)
    }

    @Test
    fun `retry should show max attempts message after exceeding manual retry limit`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = UnknownHostException("No network")

        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // When - retry 4 times (exceeding MAX_MANUAL_RETRIES = 3)
        repeat(4) {
            viewModel.retry()
            advanceUntilIdle()
        }

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(!finalState.isRetryable)
        assertTrue(finalState.message.contains("Unable to load employees after multiple attempts"))
    }

    @Test
    fun `retry should reset retry counters on success`() = runTest {
        // Given - start with error
        fakeApiService.exceptionToThrow = UnknownHostException("No network")

        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Retry once (should increment manual retry count)
        viewModel.retry()
        advanceUntilIdle()

        // Fix the issue for next retry attempt
        fakeApiService.exceptionToThrow = null
        setupSuccessfulResponse()

        // When - retry again, this should succeed and reset counters
        viewModel.retry()
        advanceUntilIdle()

        // Verify success
        assertTrue(viewModel.uiState.value is UIState.Success)

        // Now cause an error again to test that counters were reset
        fakeApiService.exceptionToThrow = UnknownHostException("No network again")

        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Should be able to retry again (counters were reset)
        fakeApiService.exceptionToThrow = null
        setupSuccessfulResponse()

        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is UIState.Success)
    }

    // ===== AUTO-RETRY TESTS =====

    @Test
    fun `should auto-retry with exponential backoff for retryable errors`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = UnknownHostException("No network")

        viewModel = EmployeeListViewModel(realRepository)

        // When
        viewModel.uiState.test {
            assertEquals(UIState.Initial, awaitItem())

            viewModel.initiateEmployeeLoading()

            assertEquals(UIState.Loading, awaitItem())
            assertEquals(UIState.Error::class, awaitItem()::class) // First error

            // Auto-retry should happen after delay
            advanceTimeBy(1000) // First retry after 1 second
            assertEquals(UIState.Loading, awaitItem())
            assertEquals(UIState.Error::class, awaitItem()::class) // Second error

            // Second auto-retry should happen after 2 seconds
            advanceTimeBy(2000) // Second retry after 2 seconds
            assertEquals(UIState.Loading, awaitItem())
            assertEquals(UIState.Error::class, awaitItem()::class) // Third error

            // No more auto-retries (MAX_AUTO_RETRIES = 2)
            expectNoEvents()
        }
    }

    @Test
    fun `should not auto-retry for non-retryable errors`() = runTest {
        // Given
        fakeApiService.exceptionToThrow = JsonDataException("Invalid JSON")

        viewModel = EmployeeListViewModel(realRepository)

        // When
        viewModel.uiState.test {
            assertEquals(UIState.Initial, awaitItem())

            viewModel.initiateEmployeeLoading()

            assertEquals(UIState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is UIState.Error)
            assertTrue(!errorState.isRetryable)

            // Should not auto-retry
            advanceTimeBy(5000) // Wait longer than retry delay
            expectNoEvents()
        }
    }

    // ===== EDGE CASE TESTS =====

    @Test
    fun `should handle rapid successive calls gracefully`() = runTest {
        // Given
        setupSuccessfulResponse()
        viewModel = EmployeeListViewModel(realRepository)

        // When - call initiateEmployeeLoading multiple times rapidly
        repeat(5) {
            viewModel.initiateEmployeeLoading()
        }
        advanceUntilIdle()

        // Then - should handle gracefully and end up in success state
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Success)
    }

    @Test
    fun `should handle changing API responses during retry attempts`() = runTest {
        // Given - start with network error
        fakeApiService.exceptionToThrow = UnknownHostException("No network")

        viewModel = EmployeeListViewModel(realRepository)
        viewModel.initiateEmployeeLoading()
        advanceUntilIdle()

        // Verify error
        assertTrue(viewModel.uiState.value is UIState.Error)

        // When - change to different error type and retry
        fakeApiService.exceptionToThrow = JsonDataException("Parse error")
        viewModel.retry()
        advanceUntilIdle()

        // Then - should handle the new error type
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UIState.Error)
        assertTrue(finalState.exception is ApiException.ParseException)
        assertTrue(!finalState.isRetryable)
    }

    // ===== INTEGRATION TESTS =====

    @Test
    fun `complete flow - network unavailable then available then success`() = runTest {
        // Given
        val networkStatusFlow = MutableSharedFlow<NetworkStatus>()
        whenever(mockNetworkManager.networkStatus).thenReturn(networkStatusFlow)

        // Start with no network
        whenever(mockNetworkManager.isNetworkAvailable).thenReturn(false)

        viewModel = EmployeeListViewModel(realRepository)

        viewModel.uiState.test {
            assertEquals(UIState.Initial, awaitItem())

            // Start loading
            viewModel.initiateEmployeeLoading()

            // Should get no network error
            assertEquals(UIState.Loading, awaitItem())
            val noNetworkError = awaitItem()
            assertTrue(noNetworkError is UIState.Error)
            assertTrue(noNetworkError.exception is ApiException.NoNetworkException)

            // Network becomes available
            whenever(mockNetworkManager.isNetworkAvailable).thenReturn(true)
            setupSuccessfulResponse()
            networkStatusFlow.emit(NetworkStatus.Available)

            // Should auto-retry and succeed
            assertEquals(UIState.Loading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is UIState.Success)
            assertEquals(2, successState.data.size)
        }
    }

    // ===== HELPER METHODS =====

    private fun setupSuccessfulResponse() {
        fakeApiService.employeesResponse = Response.success(EmployeesResponse(sampleEmployeeList))
        fakeApiService.exceptionToThrow = null
    }

    private suspend fun TestScope.advanceUntilIdle() {
        testScheduler.advanceUntilIdle()
    }
}