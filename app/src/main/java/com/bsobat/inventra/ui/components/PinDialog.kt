package com.bsobat.inventra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.R

@Composable
fun PinDialog(
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit,
    pinError: Boolean
) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.admin_pin)) },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text(stringResource(R.string.pin)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (pinError) {
                    Error(text = stringResource(R.string.invalid_pin))
                }
            }
        },
        confirmButton = {
            PrimaryButton(stringResource(R.string.ok)) {
                onPinEntered(pin)
            }
        },
        dismissButton = {
            SecondaryButton(stringResource(R.string.cancel)) {
                onDismiss()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PinDialogPreview() {
    PinDialog(
        onPinEntered = {},
        onDismiss = {},
        pinError = false
    )
}