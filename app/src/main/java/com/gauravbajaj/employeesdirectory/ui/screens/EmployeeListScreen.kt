package com.gauravbajaj.employeesdirectory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gauravbajaj.employeesdirectory.ui.base.UIState
import com.gauravbajaj.employeesdirectory.ui.components.EmployeeCard
import com.gauravbajaj.employeesdirectory.ui.components.EmptyState
import com.gauravbajaj.employeesdirectory.ui.components.ErrorState
import com.gauravbajaj.employeesdirectory.ui.viewmodel.EmployeeListViewModel

/**
 * Composable function for the EmployeeListScreen.
 *
 * This screen displays a list of employees fetched from a ViewModel.
 * It handles different UI states (Initial, Loading, Success, Error)
 * and allows retrying the data loading on error.
 * Users can click on an item in the list, triggering the [onItemClick] callback.
 *
 * @param modifier The Modifier to be applied to the root Composable of this screen.
 * @param onItemClick A callback function that is invoked when an employee item is clicked.
 *                    It receives the clicked [Employee] object as a parameter.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EmployeeListScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<EmployeeListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState is UIState.Loading,
        onRefresh = {
            viewModel.loadEmployees()
        }
    )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                UIState.Initial -> {
                    EmptyState(
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UIState.Success -> {
                    val successState = uiState as UIState.Success
                    if (successState.data.isEmpty()) {
                        EmptyState(
                            {
                                viewModel.loadEmployees()
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
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

                is UIState.Error -> {
                    val successState = uiState as UIState.Error
                    ErrorState(
                        error = successState,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // Pull to refresh would handle the loading state
                }
            }
            PullRefreshIndicator(
                refreshing = uiState is UIState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
