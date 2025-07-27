//package com.gauravbajaj.employeesdirectory.ui.viewmodel
//
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import app.cash.turbine.test
//import com.gauravbajaj.employeesdirectory.data.ApiResult
//import com.gauravbajaj.employeesdirectory.data.model.Employee
//import com.gauravbajaj.employeesdirectory.data.model.EmployeeType
//import com.gauravbajaj.employeesdirectory.data.repository.EmployeesRepository
//import com.gauravbajaj.employeesdirectory.ui.base.UIState
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.*
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.Mock
//import org.mockito.MockitoAnnotations
//import org.mockito.kotlin.whenever
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//@ExperimentalCoroutinesApi
//class EmployeeListViewModelTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Mock
//    private lateinit var repository: EmployeesRepository
//
//    private lateinit var viewModel: EmployeeListViewModel
//
//    private val sampleEmployee = Employee(
//        uuid = "test-uuid",
//        full_name = "John Doe",
//        phone_number = null,
//        email_address = "john@example.com",
//        biography = null,
//        photo_url_small = null,
//        photo_url_large = null,
//        team = "Engineering",
//        employee_type = EmployeeType.FULL_TIME
//    )
//
//    @Before
//    fun setup() {
//        MockitoAnnotations.openMocks(this)
//        Dispatchers.setMain(testDispatcher)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `should update state with employees on success`() = runTest {
//        // Given
//        whenever(repository.getEmployees()).thenReturn(
//            flowOf(ApiResult.Success(listOf(sampleEmployee)))
//        )
//
//        // When
//        viewModel = EmployeeListViewModel(repository)
//        advanceUntilIdle()
//
//        // Then
//        viewModel.uiState.test {
//            val state = awaitItem()
//            assertFalse(state is UIState.Loading)
//            assertTrue(state is UIState.Success)
//            val successState = state as UIState.Success<List<Employee>>
//            assertEquals(1, successState.data.size)
//            assertEquals(sampleEmployee, successState.data.first())
//        }
//    }
//
//    @Test
//    fun `should update state with error on failure`() = runTest {
//        // Given
//        val errorMessage = "Network error"
//        whenever(repository.getEmployees()).thenReturn(
//            flowOf(ApiResult.Error(errorMessage))
//        )
//
//        // When
//        viewModel = EmployeeListViewModel(repository)
//        advanceUntilIdle()
//
//        // Then
//        viewModel.uiState.test {
//            val state = awaitItem()
//            assertFalse(state is UIState.Loading)
//            assertTrue(state is UIState.Error)
//            val errorState = state as UIState.Error
//            assertEquals(errorMessage, errorState.message)
//        }
//    }
//}