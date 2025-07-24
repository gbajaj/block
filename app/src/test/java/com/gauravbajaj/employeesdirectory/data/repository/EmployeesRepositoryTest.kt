package com.gauravbajaj.employeesdirectory.data.repository

import app.cash.turbine.test
import com.gauravbajaj.employeesdirectory.data.ApiResult
import com.gauravbajaj.employeesdirectory.data.api.EmployeesApiService
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.model.EmployeeType
import com.gauravbajaj.employeesdirectory.data.model.EmployeesResponse
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmployeesRepositoryTest {

    @Mock
    private lateinit var apiService: EmployeesApiService

    private lateinit var repository: EmployeesRepository

    private val sampleEmployee = Employee(
        uuid = "test-uuid",
        full_name = "John Doe",
        phone_number = "1234567890",
        email_address = "john@example.com",
        biography = "Test bio",
        photo_url_small = "http://example.com/small.jpg",
        photo_url_large = "http://example.com/large.jpg",
        team = "Engineering",
        employee_type = EmployeeType.FULL_TIME
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = EmployeesRepository(apiService)
    }

    @Test
    fun `getEmployees should emit loading then success when API call succeeds`() = runTest {
        // Given
        val response = EmployeesResponse(listOf(sampleEmployee))
        whenever(apiService.getEmployees()).thenReturn(Response.success(response))

        // When & Then
        repository.getEmployees().test {
            // First emission should be Loading
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            // Second emission should be Success with data
            val successItem = awaitItem()
            assertTrue(successItem is ApiResult.Success)
            assertEquals(1, successItem.data.size)
            assertEquals(sampleEmployee, successItem.data.first())

            // No more emissions
            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit loading then error when API call fails`() = runTest {
        // Given
        whenever(apiService.getEmployees()).thenThrow(RuntimeException("Network error"))

        // When & Then
        repository.getEmployees().test {
            // First emission should be Loading
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            // Second emission should be Error
            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.message.contains("Network error"))

            // No more emissions
            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit loading then success with empty list when response is empty`() =
        runTest {
            // Given
            val emptyResponse = EmployeesResponse(emptyList())
            whenever(apiService.getEmployees()).thenReturn(Response.success(emptyResponse))

            // When & Then
            repository.getEmployees().test {
                // First emission should be Loading
                val loadingItem = awaitItem()
                assertTrue(loadingItem is ApiResult.Loading)

                // Second emission should be Success with empty list
                val successItem = awaitItem()
                assertTrue(successItem is ApiResult.Success)
                assertTrue(successItem.data.isEmpty())

                // No more emissions
                awaitComplete()
            }
        }

    @Test
    fun `getEmployees should emit loading then error when response is unsuccessful`() = runTest {
        // Given
        whenever(apiService.getEmployees()).thenReturn(
            Response.error(404, okhttp3.ResponseBody.create(null, "Not Found"))
        )

        // When & Then
        repository.getEmployees().test {
            // First emission should be Loading
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            // Second emission should be Error
            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.message.contains("Failed to load employees"))

            // No more emissions
            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should handle null response body gracefully`() = runTest {
        // Given
        whenever(apiService.getEmployees()).thenReturn(Response.success(null))

        // When & Then
        repository.getEmployees().test {
            // First emission should be Loading
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            // Second emission should be Success with empty list (null body handled)
            val successItem = awaitItem()
            assertTrue(successItem is ApiResult.Success)
            assertTrue(successItem.data.isEmpty())

            // No more emissions
            awaitComplete()
        }
    }
}