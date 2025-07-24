package com.gauravbajaj.employeesdirectory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRepository: EmployeesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<List<Employee>>>(UIState.Initial)
    val uiState: StateFlow<UIState<List<Employee>>> = _uiState

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            try {
                userRepository.getEmployees()
                    .collect { employeesResult ->

//                        _uiState.value = UIState.Success(employeesResult ?: emptyList())
                    }
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Failed to load users")
            }
        }
    }
}
