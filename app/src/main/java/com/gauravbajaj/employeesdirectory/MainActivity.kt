package com.gauravbajaj.employeesdirectory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.gauravbajaj.employeesdirectory.navigation.employeeddirectoryNavHost
import com.gauravbajaj.employeesdirectory.ui.theme.EmployeeDirectoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmployeeDirectoryTheme {
                employeeddirectoryNavHost()
            }
        }
    }
}

@Composable
fun employeesdirectoryApp() {
    employeeddirectoryNavHost()
}

@Composable
fun employeesdirectoryPreview() {
    EmployeeDirectoryTheme {
        employeesdirectoryApp()
    }
}