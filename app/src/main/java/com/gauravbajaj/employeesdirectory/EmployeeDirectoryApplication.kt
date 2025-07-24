package com.gauravbajaj.employeesdirectory

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom [Application] class for the employeedirectory application.
 *
 * This class is annotated with `@HiltAndroidApp` to enable Hilt dependency injection
 * throughout the application.
 */
@HiltAndroidApp
class EmployeeDirectoryApplication : Application()
