package com.gauravbajaj.employeesdirectory.data.repository

import android.content.Context
import com.gauravbajaj.employeesdirectory.data.ApiResult
import com.gauravbajaj.employeesdirectory.data.api.EmployeesApiService
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for fetching user data.
 *
 * This class provides methods to fetch a list of users or a single user by their ID.
 * It uses a [EmployeesApiService] to make network requests and [Moshi] for JSON parsing.
 * For development purposes, it currently uses a local JSON file ([R.raw.users]) to provide fake user data.
 *
 * @property employeesApi The API service for user-related network calls.
 * @property context The application context, used to access resources.
 * @property moshi The Moshi instance for JSON serialization and deserialization.
 */
@Singleton
class EmployeesRepository @Inject constructor(
    private val employeesApi: EmployeesApiService,
    @ApplicationContext
    private val context: Context,
    private val moshi: Moshi
) {
    fun getEmployees(): Flow<ApiResult<List<Employee>>> = flow {
        emit(ApiResult.Loading())
        try {
            val response = employeesApi.getEmployees()
            if (response.isSuccessful) {
                val employees = response.body()?.employees ?: emptyList()
                emit(ApiResult.Success(employees))
            } else {
                emit(ApiResult.Error("Failed to load employees: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Network error: ${e.message}"))
        }
    }

}
