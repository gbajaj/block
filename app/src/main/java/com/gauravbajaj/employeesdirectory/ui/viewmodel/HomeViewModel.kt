package com.gauravbajaj.employeesdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gauravbajaj.employeesdirectory.data.ApiResult
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.data.repository.EmployeesRepository
import com.gauravbajaj.employeesdirectory.ui.base.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val employeeRepository: EmployeesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<List<Employee>>>(UIState.Initial)
    val uiState: StateFlow<UIState<List<Employee>>> = _uiState

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        viewModelScope.launch {
            employeeRepository.getEmployees().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = UIState.Loading
                    }
                    is ApiResult.Success -> {
                        _uiState.value = UIState.Success (result.data)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = UIState.Error (result.message)
                    }
                }
            }
        }
    }

    fun retry() {
        loadEmployees()
    }
}
