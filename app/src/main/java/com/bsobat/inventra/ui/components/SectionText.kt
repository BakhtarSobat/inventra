package com.bsobat.inventra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.theme.AppTheme


@Composable
fun SectionText(
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    text: String
) {
    Box(
        Modifier
            .background(
                shape = MaterialTheme.shapes.medium,
                color = color
            )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            CenteredText(text = text, textColor = textColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SectionTextPreview() {
    AppTheme {
        SectionText(
            text = "Section Title"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SectionTextFarsiPreview() {
    AppTheme {
        SectionText(
            text = "عنوان بخش"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SectionTextCustomColorPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SectionText(
                text = "Default Colors"
            )
            SectionText(
                text = "Custom Background",
                color = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary
            )
            SectionText(
                text = "بخش فارسی",
                color = MaterialTheme.colorScheme.secondary,
                textColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}
