package com.gauravbajaj.employeesdirectory.data.api

import com.gauravbajaj.employeesdirectory.data.model.Employee
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface defining the API endpoints for user-related operations.
 * This interface is used by Retrofit to generate the network request implementations.
 */
interface EmployeesApiService {
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Employee

    @GET("users")
    suspend fun getUsers(): List<Employee>
}
