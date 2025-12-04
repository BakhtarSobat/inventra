package com.bsobat.inventra.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: String,
    isfarsi: Boolean = false,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    AppTheme {
        TopAppBar(
            navigationIcon = navigationIcon ?: {},
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            title = {
                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp)
                )
            },
            actions = actions
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
fun ToolbarLightPreview() {
    AppTheme(darkTheme = false) {
        Toolbar(title = "English Title", isfarsi = false)
    }
}

@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ToolbarDarkPreview() {
    AppTheme(darkTheme = true) {
        Toolbar(title = "English Title", isfarsi = false)
    }
}

@Preview(name = "Farsi Light", showBackground = true)
@Composable
fun ToolbarFarsiLightPreview() {
    AppTheme(darkTheme = false) {
        Toolbar(title = "عنوان فارسی", isfarsi = true)
    }
}

@Preview(
    name = "Farsi Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ToolbarFarsiDarkPreview() {
    AppTheme(darkTheme = true) {
        Toolbar(title = "عنوان فارسی", isfarsi = true)
    }
}

@Preview(name = "Navigation Light", showBackground = true)
@Composable
fun ToolbarWithNavigationLightPreview() {
    AppTheme(darkTheme = false) {
        Toolbar(
            title = "Title with Navigation",
            isfarsi = false,
            navigationIcon = {
                IconButton(onClick = { }) {
//                    Icon(
//                        painter = painterResource(id = AdaptiveIconDrawable.),
//                        contentDescription = "Back"
//                    )
                }
            }
        )
    }
}

@Preview(
    name = "Navigation Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)

@Composable
fun ToolbarWithNavigationDarkPreview() {
    AppTheme(darkTheme = true) {
        Toolbar(
            title = "Title with Navigation",
            navigationIcon = {
                IconButton(onClick = { }) {

                }
            }
        )
    }
}

@Preview(name = "Actions Light", showBackground = true)
@Composable
fun ToolbarWithActionsLightPreview() {
    AppTheme(darkTheme = false) {
        Toolbar(
            title = "Title with Actions",
            actions = {
                IconButton(onClick = { }) {

                }
            }
        )
    }
}

@Preview(
    name = "Actions Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ToolbarWithActionsDarkPreview() {
    AppTheme(darkTheme = true) {
        Toolbar(
            title = "Title with Actions",
            actions = {
                IconButton(onClick = { }) {
                }
            }
        )
    }
}