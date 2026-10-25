package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.feature.activity.presentation.state.CepState

@Composable
internal fun CepSearchDialog(
    state: CepState,
    onInput: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Explorar por CEP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GPS no lugar errado? Informe um CEP para centralizar o mapa nessa região.")
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInput,
                    label = { Text("CEP") },
                    placeholder = { Text("01310-100") },
                    singleLine = true,
                    enabled = !state.loading,
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(
                    "Consulta online à BrasilAPI. A posição é aproximada e não altera seu GPS.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSearch, enabled = !state.loading) {
                Text(if (state.loading) "Buscando…" else "Ver no mapa")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
