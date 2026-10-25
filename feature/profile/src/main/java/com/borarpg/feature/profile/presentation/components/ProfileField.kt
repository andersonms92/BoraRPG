package com.borarpg.feature.profile.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Rose

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
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Cream,
                unfocusedTextColor = Cream,
                disabledTextColor = Muted,
                focusedContainerColor = Elevated,
                unfocusedContainerColor = Elevated,
                disabledContainerColor = Elevated,
                errorContainerColor = Elevated,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Cream.copy(alpha = .08f),
                disabledBorderColor = Cream.copy(alpha = .04f),
                errorBorderColor = Rose,
                focusedLabelColor = Accent,
                unfocusedLabelColor = Muted,
                errorLabelColor = Rose,
                errorSupportingTextColor = Rose,
                cursorColor = Accent,
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}
