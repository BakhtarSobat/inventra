package com.bsobat.inventra.ui

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.theme.AppTheme
import com.bsobat.inventra.ui.components.PinDialog
import com.bsobat.inventra.ui.navigation.Navigation2
import com.bsobat.inventra.ui.navigation.Screen
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App(viewModel: MainActivityViewModel = koinViewModel()) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Screen>()
    val context = LocalContext.current
    val showExitDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val basketItems by viewModel.basketItemsFlow.collectAsState(initial = emptyList())
    var showPinDialog by remember { mutableStateOf(false) }
    val adminRight by viewModel.adminLoggedIn.collectAsState()
    var showConfigDialog by remember { mutableStateOf(false) }
    val syncState by viewModel.syncState.collectAsState()
    val isSignedIn = remember { mutableStateOf(viewModel.checkGoogleDriveSignInStatus()) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleDriveSignInResult(result.data)
            isSignedIn.value = true
        }
    }
    // Export file picker
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val tempFile = kotlin.io.path.createTempFile().toFile()
                    viewModel.export(tempFile.absolutePath).onSuccess {
                        tempFile.inputStream().use { input ->
                            input.copyTo(outputStream)
                        }
                        tempFile.delete()
                    }
                }
            }
        }
    }

    // Import file picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val tempFile = kotlin.io.path.createTempFile().toFile()
                    tempFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    viewModel.import(tempFile.absolutePath)
                    tempFile.delete()
                }
            }
        }
    }

    if (showExitDialog.value) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog.value = false },
            onConfirm = { (context as? Activity)?.finish() }
        )
    }

    AppTheme {
        val systemUiController: SystemUiController = rememberSystemUiController()
        val navBarColor = colorScheme.secondaryContainer
        SideEffect {
            systemUiController.setStatusBarColor(Color.Transparent)
            systemUiController.setNavigationBarColor(navBarColor)
        }
        val showMenu = remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        ShoppingCardBadge(scope, navigator, basketItems)
                        IconButton(onClick = { showMenu.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export)) },
                                onClick = {
                                    showMenu.value = false
                                    exportLauncher.launch("inventra_backup_${System.currentTimeMillis()}.zip")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_data)) },
                                onClick = {
                                    showMenu.value = false
                                    importLauncher.launch(arrayOf("application/zip"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.configuration)) },
                                onClick = {
                                    showMenu.value = false
                                    showPinDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isSignedIn.value) {
                                            stringResource(R.string.sync_with_google_drive)
                                        } else {
                                            stringResource(R.string.sign_in_to_google_drive)
                                        }
                                    )
                                },
                                onClick = {
                                    showMenu.value = false
                                    if (isSignedIn.value) {
                                        viewModel.syncWithGoogleDrive()
                                    } else {
                                        viewModel.signInToGoogleDrive()?.let { intent ->
                                            signInLauncher.launch(intent)
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sign_out_from_google_drive)) },
                                onClick = {
                                    showMenu.value = false
                                    viewModel.signOutFromGoogleDrive()
                                    isSignedIn.value = false
                                },
                                enabled = isSignedIn.value
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primaryContainer
                    )
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    Navigation2(navigator)
                }
            }
        )
    }

    if (showPinDialog) {
        if (adminRight) {
            showPinDialog = false
            showConfigDialog = true
        } else {
            PinDialog(
                onPinEntered = { pin -> viewModel.onAdminPinCheck(pin) },
                onDismiss = {
                    showPinDialog = false
                },
                pinError = false
            )
        }
    }

    if(showConfigDialog){
        showConfigDialog = false
        scope.launch {
            navigator.navigateTo(
                pane = ListDetailPaneScaffoldRole.Detail,
                contentKey = Screen.ConfigurationScreen
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun ShoppingCardBadge(
    scope: CoroutineScope,
    navigator: ThreePaneScaffoldNavigator<Screen>,
    basketItems: List<BasketItem>
) {
    IconButton(onClick = {
        scope.launch {
            navigator.navigateTo(
                pane = ListDetailPaneScaffoldRole.Detail,
                contentKey = Screen.BasketScreen,
            )
        }
    }) {
        BadgedBox(
            badge = {
                val basketCount = basketItems.size
                if (basketCount > 0 && basketCount < 10) {
                    Badge { Text(basketCount.toString()) }
                } else if (basketCount >= 10) {
                    Badge { Text("+") }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Go to basket"
            )
        }
    }
}

@Composable
private fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AppTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Exit Inventra") },
            text = { Text("Are you sure you want to exit?") },
            confirmButton = {
                Text("Yes")
                onConfirm()
            },
            dismissButton = {
                Text("Cancel")
                onDismiss()
            }
        )
    }
}