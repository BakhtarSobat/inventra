package com.bsobat.inventra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Divider(thickness: Int = 1, color: Color = MaterialTheme.colorScheme.surfaceVariant) =
    HorizontalDivider(
        modifier = Modifier.padding(
            start = 0.dp,
            end = 0.dp,
            top = 4.dp,
            bottom = 4.dp

        ), thickness = thickness.dp, color = color
    )


@Preview(showBackground = true)
@Composable
fun DividerPreview() {
    Column {
        Text("Content above divider")
        Divider()
        Text("Content below divider")
    }
}

@Preview(showBackground = true)
@Composable
fun DividerThickPreview() {
    Column {
        Text("Content above thick divider")
        Divider(thickness = 3)
        Text("Content below thick divider")
    }
}

@Preview(showBackground = true)
@Composable
fun DividerVariousThicknessPreview() {
    Column {
        Text("Thickness 1dp")
        Divider(thickness = 1)
        Text("Thickness 2dp")
        Divider(thickness = 2)
        Text("Thickness 4dp")
        Divider(thickness = 4)
        Text("Thickness 8dp")
        Divider(thickness = 8)
    }
}