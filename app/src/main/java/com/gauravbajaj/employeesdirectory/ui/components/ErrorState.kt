package com.gauravbajaj.employeesdirectory.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gauravbajaj.employeesdirectory.data.ApiException
import com.gauravbajaj.employeesdirectory.ui.base.UIState

/**
 * A composable function that displays an error message with a retry button.
 *
 * @param message The error message to display.
 * @param onRetry A callback function to be invoked when the retry button is clicked.
 * @param modifier Optional modifier for this composable.
 */
@Composable
fun ErrorState(
    error: UIState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, message) = when (error.exception) {
        is ApiException.NetworkException -> Icons.Default.WifiOff to "Connection Problem"
        is ApiException.ServerException -> Icons.Default.CloudOff to "Server Error"
        is ApiException.ParseException -> Icons.Default.Error to "Data Error"
        else -> Icons.Default.Error to "Something Went Wrong"
    }
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = error.message,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error.isRetryable) {
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        } else {
            OutlinedButton(onClick = onRetry) {
                Text("Refresh")
            }
        }
    }
}
