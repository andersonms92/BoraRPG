package com.borarpg.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.domain.formatActivityTime
import com.borarpg.ui.AdventureState
import com.borarpg.ui.theme.*

@Composable
internal fun ActivityControl(state: AdventureState, onStart: () -> Unit, onStop: () -> Unit) {
    val context = LocalContext.current
    var denied by rememberSaveable { mutableStateOf(false) }
    val permissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            result ->
            if (
                Build.VERSION.SDK_INT < 29 ||
                    result[Manifest.permission.ACTIVITY_RECOGNITION] == true
            ) {
                denied = false
                onStart()
            } else denied = true
        }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Eyebrow(if (state.tracking.active) "Tempo de atividade" else "Última atividade")
        Text(
            formatActivityTime(state.tracking.elapsedMillis),
            fontSize = 38.sp,
            fontFamily = FontFamily.Monospace,
            color = Lime,
        )
        Button(
            enabled = !state.loading && state.error == null && state.sensorAvailable,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            onClick = {
                if (state.tracking.active) onStop()
                else if (
                    Build.VERSION.SDK_INT >= 29 &&
                        context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) !=
                            PackageManager.PERMISSION_GRANTED
                ) {
                    val needed = buildList {
                        add(Manifest.permission.ACTIVITY_RECOGNITION)
                        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissions.launch(needed.toTypedArray())
                } else onStart()
            },
        ) {
            Text(
                if (state.tracking.active) "Encerrar atividade" else "Iniciar caminhada ou corrida"
            )
        }
        Text(
            when {
                !state.sensorAvailable && !state.loading ->
                    "Este aparelho não possui contador de passos compatível. Você ainda pode explorar o mapa."
                state.tracking.active ->
                    "Contando passos pelo sensor do aparelho, inclusive com a tela bloqueada. Encerre quando terminar."
                else -> "Inicie uma atividade para contar os passos feitos a partir desse momento."
            },
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        state.tracking.error?.let { Text(it, color = Coral) }
        if (denied) {
            Text("A contagem precisa da permissão de atividade física.", color = Coral)
            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}"),
                        )
                    )
                }
            ) {
                Text("Abrir permissões")
            }
        }
    }
}
