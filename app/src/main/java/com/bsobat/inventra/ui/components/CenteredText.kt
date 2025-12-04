package com.bsobat.inventra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsobat.inventra.theme.AppTheme

@Composable
fun CenteredText(text: String, textColor: Color = MaterialTheme.colorScheme.onSurface) {
    Text(text = text, textAlign = TextAlign.Center, fontSize = 16.sp, color = textColor)
}

@Preview(showBackground = true)
@Composable
fun CenteredTextPreview() {
    MaterialTheme {
        CenteredText(
            text = "Centered Text Example"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CenteredTextFarsiPreview() {
    MaterialTheme {
        CenteredText(
            text = "Test"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CenteredTextMultiplePreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CenteredText(
                text = "Default Color"
            )
            CenteredText(
                text = "Custom Color",
                textColor = MaterialTheme.colorScheme.primary
            )
            CenteredText(
                text = "Test"
            )
        }
    }
}

