package com.gauravbajaj.employeesdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gauravbajaj.employeesdirectory.data.ApiException
import com.gauravbajaj.employeesdirectory.data.ApiResult
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.network.NetworkStatus
import com.gauravbajaj.employeesdirectory.data.repository.EmployeesRepository
import com.gauravbajaj.employeesdirectory.ui.base.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A [ViewModel] responsible for loading the list of employees from the API
 * and updating the UI state accordingly.
 *
 * This ViewModel uses [EmployeesRepository] to fetch the list of employees
 * and [StateFlow] to maintain and update the UI state.
 *
 * The [uiState] StateFlow can be observed by Activities or Fragments to
 * react to changes in the UI state.
 */
@HiltViewModel
class EmployeeListViewModel @Inject constructor(
    private val employeeRepository: EmployeesRepository
) : ViewModel() {

    private var autoRetryCount = 0
    private var manualRetryCount = 0
    private val _uiState = MutableStateFlow<UIState<List<Employee>>>(UIState.Initial)
    val uiState: StateFlow<UIState<List<Employee>>> = _uiState

    // Separate StateFlow for network status
    private val _networkStatus = MutableStateFlow(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            employeeRepository.getNetworkStatus().collect { networkStatus ->
                _networkStatus.value = networkStatus

                // Auto-retry if network becomes available and we have a network error
                if (networkStatus == NetworkStatus.Available) {
                    val currentState = _uiState.value
                    if (currentState is UIState.Error && currentState.exception is ApiException.NoNetworkException) {
                        autoRetryCount = 0 // Reset retry count
                        loadEmployees()
                    }
                }
            }
        }
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            employeeRepository.getEmployees().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = UIState.Loading
                    }

                    is ApiResult.Success -> {
                        autoRetryCount = 0 // Reset retry count on success
                        manualRetryCount = 0 // Reset manual retry count on success
                        _uiState.value = UIState.success(result.data)
                    }

                    is ApiResult.Error -> {
                        _uiState.value = UIState.error(result.exception)
                        autoRetryIfApplicable()
                    }
                }
            }
        }
    }

    fun retry() {
        if (manualRetryCount < MAX_MANUAL_RETRIES) {
            manualRetryCount++
            autoRetryCount = 0  // Reset auto-retry when user manually retries
            loadEmployees()
        } else {
            val currentState = _uiState.value
            if (currentState is UIState.Error) {
                _uiState.value = UIState.error(
                    currentState.exception,
                    allowRetry = false,
                    message = "Unable to load employees after multiple attempts. Please check your connection and try again later."
                )
            }
        }
    }

    // Auto-retry for network errors with exponential backoff
    private fun autoRetryIfApplicable() {
        val currentError = _uiState.value as? UIState.Error
        if (currentError?.isRetryable == true && autoRetryCount < MAX_AUTO_RETRIES) {
            viewModelScope.launch {
                delay(AUTO_RETRY_DELAY_MS * (autoRetryCount + 1)) // 1s, 2s delays
                autoRetryCount++
                loadEmployees()
            }
        }
    }

    /**
     * Calls observeNetworkStatus and loadEmployees together to initiate both flows.
     */
    fun initiateEmployeeLoading() {
        observeNetworkStatus()
        loadEmployees()
    }

    companion object {
        private const val MAX_AUTO_RETRIES = 2      // Auto-retry: 2 attempts
        private const val MAX_MANUAL_RETRIES = 3    // Manual retry: 3 attempts
        private const val AUTO_RETRY_DELAY_MS = 1000L
    }
}
