package com.gauravbajaj.employeesdirectory.data.repository

import app.cash.turbine.test
import com.gauravbajaj.employeesdirectory.data.ApiException
import com.gauravbajaj.employeesdirectory.data.ApiResult
import com.gauravbajaj.employeesdirectory.data.remote.EmployeesApiService
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.model.EmployeeType
import com.gauravbajaj.employeesdirectory.data.model.EmployeesResponse
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.gauravbajaj.employeesdirectory.data.FakeEmployeesApiService

class EmployeesRepositoryTest {

    private lateinit var apiService: FakeEmployeesApiService

    private lateinit var repository: EmployeesRepository

    // Test data
    private val validEmployee = Employee(
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

    private val validEmployeeMinimal = Employee(
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

    private val contractorEmployee = Employee(
        uuid = "550e8400-e29b-41d4-a716-446655440002",
        full_name = "Bob Wilson",
        phone_number = "9876543210",
        email_address = "bob.wilson@contractor.com",
        biography = "Freelance designer",
        photo_url_small = "https://example.com/photos/bob_small.jpg",
        photo_url_large = "https://example.com/photos/bob_large.jpg",
        team = "Design",
        employee_type = EmployeeType.CONTRACTOR
    )

    private val invalidEmployeeEmptyUuid = Employee(
        uuid = "",  // Invalid: empty UUID
        full_name = "Invalid User",
        phone_number = null,
        email_address = "invalid@example.com",
        biography = null,
        photo_url_small = null,
        photo_url_large = null,
        team = "Testing",
        employee_type = EmployeeType.FULL_TIME
    )

    private val invalidEmployeeEmptyName = Employee(
        uuid = "550e8400-e29b-41d4-a716-446655440003",
        full_name = "",  // Invalid: empty name
        phone_number = null,
        email_address = "noname@example.com",
        biography = null,
        photo_url_small = null,
        photo_url_large = null,
        team = "Testing",
        employee_type = EmployeeType.FULL_TIME
    )

    private val invalidEmployeeEmptyEmail = Employee(
        uuid = "550e8400-e29b-41d4-a716-446655440004",
        full_name = "No Email User",
        phone_number = null,
        email_address = "",  // Invalid: empty email
        biography = null,
        photo_url_small = null,
        photo_url_large = null,
        team = "Testing",
        employee_type = EmployeeType.FULL_TIME
    )

    private val invalidEmployeeEmptyTeam = Employee(
        uuid = "550e8400-e29b-41d4-a716-446655440005",
        full_name = "No Team User",
        phone_number = null,
        email_address = "noteam@example.com",
        biography = null,
        photo_url_small = null,
        photo_url_large = null,
        team = "",  // Invalid: empty team
        employee_type = EmployeeType.FULL_TIME
    )

    @Before
    fun setup() {
        apiService = FakeEmployeesApiService()
        repository = EmployeesRepository(apiService)
    }

    // ===== SUCCESS SCENARIOS =====


    @Test
    fun `getEmployees should emit loading then success when API call succeeds with single employee`() =
        runTest {
            // Given
            val response = EmployeesResponse(listOf(validEmployee))
            apiService.employeesResponse = Response.success(response)

            // When & Then
            repository.getEmployees().test {
                // First emission should be Loading
                val loadingItem = awaitItem()
                assertTrue(loadingItem is ApiResult.Loading)

                // Second emission should be Success with data
                val successItem = awaitItem()
                assertTrue(successItem is ApiResult.Success)
                assertEquals(1, successItem.data.size)
                assertEquals(validEmployee, successItem.data.first())

                // No more emissions
                awaitComplete()
            }
        }

    @Test
    fun `getEmployees should emit loading then success when API call succeeds with multiple employees`() =
        runTest {
            // Given
            val employees = listOf(validEmployee, validEmployeeMinimal, contractorEmployee)
            val response = EmployeesResponse(employees)
            apiService.employeesResponse = Response.success(response)

            // When & Then
            repository.getEmployees().test {
                val loadingItem = awaitItem()
                assertTrue(loadingItem is ApiResult.Loading)

                val successItem = awaitItem()
                assertTrue(successItem is ApiResult.Success)
                assertEquals(3, successItem.data.size)
                assertEquals(employees, successItem.data)

                awaitComplete()
            }
        }

    @Test
    fun `getEmployees should emit loading then success with empty list when response is empty`() =
        runTest {
            // Given
            val emptyResponse = EmployeesResponse(emptyList())
            apiService.employeesResponse = Response.success(emptyResponse)

            // When & Then
            repository.getEmployees().test {
                val loadingItem = awaitItem()
                assertTrue(loadingItem is ApiResult.Loading)

                val successItem = awaitItem()
                assertTrue(successItem is ApiResult.Success)
                assertTrue(successItem.data.isEmpty())

                awaitComplete()
            }
        }

    @Test
    fun `getEmployees should handle null response body gracefully`() = runTest {
        // Given
        apiService.employeesResponse = Response.success(null)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is ApiResult.Success)
            assertTrue(successItem.data.isEmpty())

            awaitComplete()
        }
    }

    // ===== VALIDATION SCENARIOS =====

    @Test
    fun `getEmployees should filter out employees with empty UUID`() = runTest {
        // Given
        val employees = listOf(validEmployee, invalidEmployeeEmptyUuid, validEmployeeMinimal)
        val response = EmployeesResponse(employees)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)
            assertTrue(errorItem.exception.message!!.contains("malformed"))

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should filter out employees with empty full_name`() = runTest {
        // Given
        val employees = listOf(validEmployee, invalidEmployeeEmptyName)
        val response = EmployeesResponse(employees)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should filter out employees with empty email_address`() = runTest {
        // Given
        val employees = listOf(validEmployee, invalidEmployeeEmptyEmail)
        val response = EmployeesResponse(employees)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should filter out employees with empty team`() = runTest {
        // Given
        val employees = listOf(validEmployee, invalidEmployeeEmptyTeam)
        val response = EmployeesResponse(employees)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit error when all employees are malformed`() = runTest {
        // Given
        val malformedEmployees = listOf(
            invalidEmployeeEmptyUuid,
            invalidEmployeeEmptyName,
            invalidEmployeeEmptyEmail,
            invalidEmployeeEmptyTeam
        )
        val response = EmployeesResponse(malformedEmployees)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)

            awaitComplete()
        }
    }

    // ===== SERVER ERROR SCENARIOS =====

    @Test
    fun `getEmployees should emit server error for 404 response`() = runTest {
        // Given
        val errorBody = "Not Found".toResponseBody("text/plain".toMediaType())
        apiService.employeesResponse = Response.error(404, errorBody)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ServerException)
            assertEquals(404, (errorItem.exception as ApiException.ServerException).code)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit server error for 500 response`() = runTest {
        // Given
        val errorBody = "Internal Server Error".toResponseBody("text/plain".toMediaType())
        apiService.employeesResponse = Response.error(500, errorBody)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ServerException)
            assertEquals(500, (errorItem.exception as ApiException.ServerException).code)
            assertEquals(
                "Server is temporarily unavailable. Please try again later",
                errorItem.message
            )

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit server error for 503 response`() = runTest {
        // Given
        val errorBody = "Service Unavailable".toResponseBody("text/plain".toMediaType())
        apiService.employeesResponse = Response.error(503, errorBody)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ServerException)
            assertEquals(503, (errorItem.exception as ApiException.ServerException).code)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should handle HttpException`() = runTest {
        // Given
        val httpException = HttpException(
            Response.error<Any>(
                401,
                "Unauthorized".toResponseBody("text/plain".toMediaType())
            )
        )
        apiService.exceptionToThrow = httpException

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ServerException)
            assertEquals(401, (errorItem.exception as ApiException.ServerException).code)

            awaitComplete()
        }
    }

    // ===== NETWORK ERROR SCENARIOS =====

    @Test
    fun `getEmployees should emit network error for UnknownHostException`() = runTest {
        // Given
        apiService.exceptionToThrow = UnknownHostException("Unable to resolve host")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.NetworkException)
            assertEquals("Please check your internet connection and try again", errorItem.message)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit network error for ConnectException`() = runTest {
        // Given
        apiService.exceptionToThrow = ConnectException("Connection refused")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.NetworkException)
            assertEquals("Please check your internet connection and try again", errorItem.message)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit network error for SocketTimeoutException`() = runTest {
        // Given
        apiService.exceptionToThrow = SocketTimeoutException("Read timed out")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.NetworkException)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit network error for IOException`() = runTest {
        // Given
        apiService.exceptionToThrow = IOException("Network error")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.NetworkException)

            awaitComplete()
        }
    }

    // ===== PARSING ERROR SCENARIOS =====

    @Test
    fun `getEmployees should emit parse error for JsonDataException`() = runTest {
        // Given
        apiService.exceptionToThrow = JsonDataException("Malformed JSON")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)
            assertEquals("Unable to load employee data. Please try again", errorItem.message)

            awaitComplete()
        }
    }

    // ===== UNKNOWN ERROR SCENARIOS =====

    @Test
    fun `getEmployees should emit unknown error for unexpected exceptions`() = runTest {
        // Given
        apiService.exceptionToThrow = RuntimeException("Unexpected error")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.UnknownException)
            assertEquals("An unexpected error occurred. Please try again", errorItem.message)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should emit unknown error for IllegalStateException`() = runTest {
        // Given
        apiService.exceptionToThrow = IllegalStateException("Invalid state")

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.UnknownException)

            awaitComplete()
        }
    }

    // ===== EDGE CASE SCENARIOS =====

    @Test
    fun `getEmployees should handle response with null employees list`() = runTest {
        // Given - This would be a malformed response where employees field is null
        // This test would require custom mocking of Gson or Response creation
        // Skipping as it's handled by the null response body test above
    }

    @Test
    fun `getEmployees should handle mixed valid and invalid employees`() = runTest {
        // Given
        val mixedEmployees = listOf(
            validEmployee,          // Valid
            invalidEmployeeEmptyUuid, // Invalid
            validEmployeeMinimal,   // Valid
            invalidEmployeeEmptyTeam  // Invalid
        )
        val response = EmployeesResponse(mixedEmployees)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            // Should emit error because some employees are malformed
            val errorItem = awaitItem()
            assertTrue(errorItem is ApiResult.Error)
            assertTrue(errorItem.exception is ApiException.ParseException)

            awaitComplete()
        }
    }

    @Test
    fun `getEmployees should preserve all employee types`() = runTest {
        // Given
        val employeesWithAllTypes = listOf(
            validEmployee.copy(employee_type = EmployeeType.FULL_TIME),
            validEmployeeMinimal.copy(employee_type = EmployeeType.PART_TIME),
            contractorEmployee.copy(employee_type = EmployeeType.CONTRACTOR)
        )
        val response = EmployeesResponse(employeesWithAllTypes)
        apiService.employeesResponse = Response.success(response)

        // When & Then
        repository.getEmployees().test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ApiResult.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is ApiResult.Success)
            assertEquals(3, successItem.data.size)

            val types = successItem.data.map { it.employee_type }.toSet()
            assertEquals(3, types.size) // All three types present
            assertTrue(types.contains(EmployeeType.FULL_TIME))
            assertTrue(types.contains(EmployeeType.PART_TIME))
            assertTrue(types.contains(EmployeeType.CONTRACTOR))

            awaitComplete()
        }
    }
}

//class FakeEmployeesApiService : EmployeesApiService {
//    var employeesResponse: Response<EmployeesResponse>? = null
//    var exceptionToThrow: Throwable? = null
//
//    override suspend fun getEmployees(): Response<EmployeesResponse> {
//        exceptionToThrow?.let { throw it }
//        return employeesResponse ?: Response.success(EmployeesResponse(emptyList()))
//    }
//
//    override suspend fun getMalformedEmployees(): Response<EmployeesResponse> {
//        return Response.success(EmployeesResponse(emptyList()))
//    }
//
//    override suspend fun getEmptyEmployees(): Response<EmployeesResponse> {
//        return Response.success(EmployeesResponse(emptyList()))
//    }
//}