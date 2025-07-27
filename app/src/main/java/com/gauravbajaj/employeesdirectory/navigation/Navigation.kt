package com.gauravbajaj.employeesdirectory.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gauravbajaj.employeesdirectory.ui.screens.EmployeeListScreen

/**
 * Composable function that defines the navigation graph for the employeedirectory app.
 *
 * This function sets up the NavHost with a single destination:
 * - [Screen.Home]: The home screen of the app, displaying a list of users.
 *
 * @param navController The [NavHostController] used to manage navigation within the app.
 *                      Defaults to a new controller remembered by [rememberNavController].
 * @param startDestination The route of the initial screen to be displayed.
 *                         Defaults to [Screen.Home.route], which is the home screen of the app.
 */
@Composable
fun employeeddirectoryNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            EmployeeListScreen()
        }
        // Add a composable for the Details screen or the screen you want to navigate to
    }
}
