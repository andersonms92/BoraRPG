package com.borarpg.feature.activity.presentation.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import com.borarpg.feature.activity.presentation.components.WorkoutPanel
import com.borarpg.feature.activity.presentation.state.WorkoutUiState

@Composable
internal fun WorkoutControls(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    onFollow: () -> Unit,
    onHistory: (String) -> Unit,
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    fun start() {
        error =
            when {
                !context
                    .getSystemService(LocationManager::class.java)
                    .isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    "Ative o GPS para registrar o percurso."
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED ->
                    "Autorize a localização precisa para registrar o percurso."
                else -> null
            }
        if (error != null) return
        onFollow()
        onAction(
            if (state.current?.status == WorkoutStatus.PAUSED) WorkoutAction.Resume
            else WorkoutAction.Start
        )
    }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            start()
        }
    WorkoutPanel(
        state.copy(message = error ?: state.message),
        onAction = onAction,
        requestStart = {
            val permissions =
                mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            if (Build.VERSION.SDK_INT >= 29)
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permission.launch(permissions.toTypedArray())
        },
        onHistory = onHistory,
    )
}
