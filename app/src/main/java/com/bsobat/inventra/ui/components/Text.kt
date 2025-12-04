package com.bsobat.inventra.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.bsobat.inventra.theme.MyTypography
import androidx.compose.material3.Text as M3Text

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Left,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MyTypography.bodyLarge,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        M3Text(
            text = text,
            color = color,
            textAlign = textAlign,
            style = style,
            modifier = modifier,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextEnglishPreview() {
    Text(text = "This is english", maxLines = 1, overflow = TextOverflow.Ellipsis)
}