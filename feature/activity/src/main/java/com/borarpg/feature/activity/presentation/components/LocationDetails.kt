package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (cep.target != null) "Região por CEP · GPS não acompanha"
            else if (locationGranted && gps.location != null) "Localização conectada"
            else "Explore no seu ritmo",
            color = Accent,
            style = MaterialTheme.typography.labelMedium,
        )
        Surface(color = Elevated.copy(alpha = .6f), shape = RoundedCornerShape(16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (cep.target == null) Icons.Outlined.MyLocation else Icons.Outlined.Place,
                    null,
                    Modifier.size(22.dp),
                    tint = Accent,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        cep.target?.let { "CEP ${it.cep}" } ?: gps.message,
                        color = Cream,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    cep.target?.let { target ->
                        Text(
                            target.address,
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        if (cep.target != null)
            cep.objectivesMessage?.let { message ->
                Text(message, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        Text(
            "Toque nos pontos para explorar. Segure o mapa para marcar um objetivo pessoal.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        if (!locationGranted)
            OutlinedButton(onClick = onPermission, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.MyLocation, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (permissionRequested) "Permissões" else "Ativar GPS")
            }
        else if (gps.location == null)
            OutlinedButton(onClick = onLocationSettings, shape = RoundedCornerShape(14.dp)) {
                Text("Configurar GPS")
            }
        Text(
            if (!hasQuests) "Nenhum objetivo salvo. Segure um local no mapa para criar o primeiro."
            else "O percurso é gravado durante uma atividade iniciada.",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}
