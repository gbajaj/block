package com.gauravbajaj.employeesdirectory.navigation

/**
 * Represents the different screens in the application.
 *
 * Each screen is defined as an object that inherits from the sealed class `Screen`.
 * The `route` property is used for navigation between screens.
 *
 * @property route The unique route identifier for the screen.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
}
