package com.bsobat.inventra.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bsobat.inventra.theme.MyTypography

@Composable
fun Title(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MyTypography.titleLarge,
        modifier = modifier
    )

}

@Preview(showBackground = true)
@Composable
fun TitlePreview() {
    Title(text = "")
}