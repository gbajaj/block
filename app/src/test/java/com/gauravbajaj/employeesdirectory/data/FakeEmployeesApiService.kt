package com.gauravbajaj.employeesdirectory.data

import com.gauravbajaj.employeesdirectory.data.model.EmployeesResponse
import com.gauravbajaj.employeesdirectory.data.remote.EmployeesApiService
import retrofit2.Response
/**
 * A fake implementation of [EmployeesApiService] used for testing.
 *
 * By default, it returns a successful response with an empty list of employees.
 *
 * You can customize the response by setting the [employeesResponse] property.
 *
 * You can also throw an exception by setting the [exceptionToThrow] property.
 */
class FakeEmployeesApiService : EmployeesApiService {
    var employeesResponse: Response<EmployeesResponse>? = null
    var exceptionToThrow: Throwable? = null

    override suspend fun getEmployees(): Response<EmployeesResponse> {
        exceptionToThrow?.let { throw it }
        return employeesResponse ?: Response.success(EmployeesResponse(emptyList()))
    }

    override suspend fun getMalformedEmployees(): Response<EmployeesResponse> {
        return Response.success(EmployeesResponse(emptyList()))
    }

    override suspend fun getEmptyEmployees(): Response<EmployeesResponse> {
        return Response.success(EmployeesResponse(emptyList()))
    }
}