package com.example.audioclasification.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permissions: List<String>,
    permissionsNotGranted: @Composable () -> Unit,
    permissionsNotAvailable: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions = permissions)

    when{
        multiplePermissionsState.allPermissionsGranted -> {
            content()
        }
        multiplePermissionsState.shouldShowRationale -> {
            permissionsNotGranted()
        }
        else -> {
            LaunchedEffect(Unit) {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
            permissionsNotAvailable()
        }
    }
}