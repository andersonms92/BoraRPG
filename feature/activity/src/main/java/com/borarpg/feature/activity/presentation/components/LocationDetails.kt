package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.domain.model.location.GpsState
import com.borarpg.feature.activity.presentation.state.CepState

@Composable
internal fun LocationDetails(
    cep: CepState,
    gps: GpsState,
    locationGranted: Boolean,
    permissionRequested: Boolean,
    hasQuests: Boolean,
    onPermission: () -> Unit,
    onLocationSettings: () -> Unit,
) {
    Eyebrow(
        if (cep.target != null) "Região por CEP · GPS não acompanha"
        else if (locationGranted && gps.location != null) "Localização conectada"
        else "Explore no seu ritmo"
    )
    Text(
        cep.target?.let { "CEP ${it.cep}" } ?: gps.message,
        style = MaterialTheme.typography.titleMedium,
    )
    cep.target?.let {
        Text(
            it.address,
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        cep.objectivesMessage?.let { message ->
            Text(
                message,
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    Text(
        "Toque nos pontos para explorar. Segure o mapa para marcar um objetivo pessoal.",
        style = MaterialTheme.typography.bodySmall,
        color = Muted,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!locationGranted)
            TextButton(
                onClick = {
                    onPermission()
                }
            ) {
                Text(if (permissionRequested) "Permissões" else "Ativar GPS")
            }
        else if (gps.location == null)
            TextButton(
                onClick = {
                    onLocationSettings()
                }
            ) {
                Text("Configurar GPS")
            }
    }
    Text(
        if (!hasQuests) "Nenhum objetivo salvo. Segure um local no mapa para criar o primeiro."
        else "O percurso é gravado durante uma atividade iniciada.",
        style = MaterialTheme.typography.labelSmall,
        color = Muted,
    )
}
