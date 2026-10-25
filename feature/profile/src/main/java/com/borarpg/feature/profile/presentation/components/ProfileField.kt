package com.borarpg.feature.profile.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Muted

@Composable
internal fun ProfileField(
    label: String,
    value: String,
    error: String?,
    keyboard: KeyboardType,
    enabled: Boolean,
    unit: String? = null,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        suffix = unit?.let { { Text(it, color = Muted) } },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}
