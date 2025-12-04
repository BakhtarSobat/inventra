package com.bsobat.inventra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.theme.AppTheme

@Composable
fun Subtitle(
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Preview(showBackground = true)
@Composable
fun SubtitlePreview() {
    AppTheme {
        Subtitle(
            text = "English Subtitle"
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SubtitleVariationsPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Subtitle(
                text = "Default English Subtitle"
            )
            Subtitle(
                text = "Long subtitle text to test how it handles multiple lines"
            )
        }
    }
}
