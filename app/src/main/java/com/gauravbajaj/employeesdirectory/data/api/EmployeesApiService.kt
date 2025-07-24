package com.gauravbajaj.employeesdirectory.data.api

import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.model.EmployeesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface defining the API endpoints for user-related operations.
 * This interface is used by Retrofit to generate the network request implementations.
 */
interface EmployeesApiService {
    @GET("employees.json")
    suspend fun getEmployees(): Response<EmployeesResponse>

    @GET("employees_malformed.json")
    suspend fun getMalformedEmployees(): Response<EmployeesResponse>


    @GET("employees_empty.json")
    suspend fun getEmptyEmployees(): Response<EmployeesResponse>
}
