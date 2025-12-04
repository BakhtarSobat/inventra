package com.bsobat.inventra.ui.components

import android.content.res.Configuration
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bsobat.inventra.theme.AppTheme

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, click: () -> Unit) {
    Button(
        onClick = { click() },
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text.uppercase())
    }
}

class ButtonTextProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("Login", "Submit", "Cancel")
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun PrimaryButtonLightPreview(
    @PreviewParameter(ButtonTextProvider::class) text: String
) {
    AppTheme(darkTheme = false, dynamicColor = false) {
        PrimaryButton(text = text) {}
    }
}

@Preview(name = "Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrimaryButtonDarkPreview(
    @PreviewParameter(ButtonTextProvider::class) text: String
) {
    AppTheme(darkTheme = true, dynamicColor = false) {
        PrimaryButton(text = text) {}
    }
}