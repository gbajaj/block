package com.gauravbajaj.employeesdirectory.data.repository

import com.gauravbajaj.employeesdirectory.data.ApiResult
import com.gauravbajaj.employeesdirectory.data.remote.EmployeesApiService
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

import com.gauravbajaj.employeesdirectory.data.ApiException
import com.gauravbajaj.employeesdirectory.data.network.NetworkConnectivityManager
import com.squareup.moshi.JsonDataException

import android.util.Log

/**
 * Repository for fetching user data.
 *
 * This class provides methods to fetch a list of users or a single user by their ID.
 * It uses a [EmployeesApiService] to make network requests and [Moshi] for JSON parsing.
 * For development purposes, it currently uses a local JSON file ([R.raw.users]) to provide fake user data.
 *
 * @property employeesApi The API service for user-related network calls.
 * @property context The application context, used to access resources.
 */
@Singleton
class EmployeesRepository @Inject constructor(
    private val employeesApi: EmployeesApiService,
    private val networkConnectivityManager: NetworkConnectivityManager
) {
    fun getEmployees(): Flow<ApiResult<List<Employee>>> = flow {
        emit(ApiResult.Loading())
        // Check network connectivity first
        if (!networkConnectivityManager.isNetworkAvailable) {
            emit(ApiResult.Error(ApiException.NoNetworkException))
            return@flow
        }
        try {
            val response = employeesApi.getEmployees()
            if (response.isSuccessful) {
                val employees = response.body()?.employees ?: emptyList()

                // Validate employee data
                val validEmployees = employees.filter { employee ->
                    employee.uuid.isNotBlank() &&
                            employee.full_name.isNotBlank() &&
                            employee.email_address.isNotBlank() &&
                            employee.team.isNotBlank()
                }

                if (validEmployees.size != employees.size) {
                    val invalidCount = employees.size - validEmployees.size
                    emit(
                        ApiResult.Error(
                            ApiException.ParseException(
                                IllegalStateException("$invalidCount employee records are malformed")
                            )
                        )
                    )
                    return@flow
                }
                emit(ApiResult.Success(validEmployees))
            } else {
                emit(
                    ApiResult.Error(
                        ApiException.ServerException(
                            code = response.code(),
                            serverMessage = response.message()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            val apiException = when (e) {
                is HttpException -> ApiException.ServerException(
                    code = e.code(),
                    serverMessage = e.message()
                )

                is UnknownHostException,
                is ConnectException -> ApiException.NetworkException(e)

                is SocketTimeoutException -> ApiException.NetworkException(e)
                is IOException -> ApiException.NetworkException(e)
                is JsonDataException -> ApiException.ParseException(e)
                else -> ApiException.UnknownException(e)
            }
            emit(ApiResult.Error(apiException))
        }
    }

    fun getNetworkStatus() = networkConnectivityManager.networkStatus

    companion object {
        private const val TAG = "EmployeesRepository"
    }
}
