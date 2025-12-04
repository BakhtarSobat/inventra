package com.bsobat.inventra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.theme.AppTheme

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, click: () -> Unit) {
    Button(
        onClick = { click() },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text(text = text.uppercase(), color = MaterialTheme.colorScheme.onSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun SecondaryButtonPreview() {
    AppTheme {
        SecondaryButton(
            text = "Click Me",
            click = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecondaryButtonFarsiPreview() {
    AppTheme {
        SecondaryButton(
            text = "Test",
            click = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecondaryButtonLongTextPreview() {
    AppTheme {
        SecondaryButton(
            text = "This is a longer button text",
            click = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecondaryButtonMultiplePreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = "Test",
                click = { }
            )
            SecondaryButton(
                text = "Test 2",
                click = { }
            )
            SecondaryButton(
                text = "Test 3",
                click = { }
            )
        }
    }
}