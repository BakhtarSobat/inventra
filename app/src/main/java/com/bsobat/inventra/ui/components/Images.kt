package com.bsobat.inventra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bsobat.inventra.R
import com.bsobat.inventra.theme.largeImage
import com.bsobat.inventra.theme.smallImage

@Composable
fun SmallImage(url: String?, modifier: Modifier = Modifier) {
    if (url.isNullOrBlank()) {
        PlaceholderImage(modifier)
    } else {
        AsyncImage(
            model = url,
            contentDescription = stringResource(R.string.selected_image),
            modifier = Modifier
                .size(smallImage)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@Composable
fun LargeImage(url: String?, modifier: Modifier = Modifier) {
    if (url.isNullOrBlank()) {
        PlaceholderImage(modifier)
    } else {
        AsyncImage(
            model = url,
            contentDescription = stringResource(R.string.selected_image),
            modifier = Modifier
                .size(largeImage)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}


@Composable
private fun PlaceholderImage(modifier: Modifier) {
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surface
                )
            ),
            shape = RoundedCornerShape(12.dp)
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}