package com.bsobat.inventra.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.bsobat.inventra.theme.MyTypography

@Composable
fun Error(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Left,
    color: Color = MaterialTheme.colorScheme.error,
    style: TextStyle = MyTypography.bodyLarge
) {
    Text(
        text = text,
        color = color,
        textAlign = textAlign,
        style = style,
        modifier = modifier
    )
}