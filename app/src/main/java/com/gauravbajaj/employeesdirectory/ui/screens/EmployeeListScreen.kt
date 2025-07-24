package com.gauravbajaj.employeesdirectory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gauravbajaj.employeesdirectory.data.model.Employee
import com.gauravbajaj.employeesdirectory.ui.base.ScreenContent
import com.gauravbajaj.employeesdirectory.ui.base.UIState
import com.gauravbajaj.employeesdirectory.ui.components.EmployeeCard
import com.gauravbajaj.employeesdirectory.ui.components.EmptyState
import com.gauravbajaj.employeesdirectory.ui.viewmodel.EmployeeListViewModel

/**
 * Composable function for the Home Screen.
 *
 * This screen displays a list of users fetched from a ViewModel.
 * It handles different UI states (Initial, Loading, Success, Error)
 * and allows retrying the data loading on error.
 * Users can click on an item in the list, triggering the [onItemClick] callback.
 *
 * @param navController The NavController used for navigation.
 * @param modifier The Modifier to be applied to the root Composable of this screen.
 * @param onItemClick A callback function that is invoked when a user item is clicked.
 *                    It receives the clicked [User] object as a parameter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (Employee) -> Unit = {}
) {
    val viewModel = hiltViewModel<EmployeeListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState) {
        if (uiState is UIState.Initial) {
            viewModel.loadEmployees()
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Block Employees Directory",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )

            )
        }
    ) { paddingValues ->
        ScreenContent(
            uiState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onRetry = {
                viewModel.loadEmployees()
            }
        ) {

            val successState = uiState as UIState.Success
            if (successState.data.isEmpty()) {
                EmptyState({
                    viewModel.loadEmployees()
                })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = successState.data,
                        key = { it.uuid }
                    ) { employee ->
                        EmployeeCard(employee = employee)
                    }
                }
            }
        }
    }
}
