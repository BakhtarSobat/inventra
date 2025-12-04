package com.bsobat.inventra.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.bsobat.inventra.theme.MyTypography

@Composable
fun Subtext(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Left,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MyTypography.bodyLarge
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(
            text = text,
            color = color,
            textAlign = textAlign,
            style = style,
            modifier = modifier.alpha(0.8f)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SubtextPreview() {
    Subtext(text = "This is english")
}