package com.bsobat.inventra.ui.products

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.Product
import com.bsobat.inventra.theme.AppTheme
import com.bsobat.inventra.ui.components.Error
import com.bsobat.inventra.ui.components.FullScreenLoading
import com.bsobat.inventra.ui.components.LargeImage
import com.bsobat.inventra.ui.components.PinDialog
import com.bsobat.inventra.ui.components.PrimaryButton
import com.bsobat.inventra.ui.components.SecondaryButton
import com.bsobat.inventra.ui.components.SmallImage
import com.bsobat.inventra.ui.components.Spacer
import com.bsobat.inventra.ui.components.Subtitle
import com.bsobat.inventra.ui.components.Text
import com.bsobat.inventra.ui.components.Title
import com.bsobat.inventra.ui.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ProductScreen(
    inputParam: Screen.ProductsScreen,
    modifier: Modifier = Modifier,
    onGoToDetail: ((Product) -> Unit)? = null
) {
    val viewModel: ProductsViewModel = koinViewModel()
    LaunchedEffect(inputParam) {
        viewModel.loadProducts(inputParam)
    }
    val uiState by viewModel.uiState.collectAsState()
    val adminRight by viewModel.adminLoggedIn.collectAsState()
    val pinError by viewModel.pinError.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editProduct by remember { mutableStateOf<Product?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is ProductsUiState.Loading -> FullScreenLoading()
            is ProductsUiState.Error -> {
                val e = uiState as ProductsUiState.Error
                Error("Error: ${e.message}")
            }

            is ProductsUiState.Success -> {
                val s = uiState as ProductsUiState.Success
                ProductContent(
                    products = s.products,
                    onGoToDetail = onGoToDetail,
                    onEdit = { p ->
                        editProduct = p
                        showPinDialog = true
                    },
                )
            }
        }

        FloatingActionButton(
            onClick = {
                editProduct = null
                showPinDialog = true
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_product))
        }
    }

    if (showPinDialog) {
        if (adminRight) {
            showPinDialog = false
            showAddEditDialog = true
        } else {
            PinDialog(
                onPinEntered = { pin -> viewModel.onAdminPinCheck(pin) },
                onDismiss = { showPinDialog = false; viewModel.clearPinError() },
                pinError = pinError
            )
        }
    }

    if (showAddEditDialog) {
        AddOrEditProductDialog(
            product = editProduct,
            onAdd = { title, desc, image ->
                val p = Product(
                    productId = UUID.randomUUID().toString(),
                    title = title,
                    description = desc.ifBlank { null },
                    image = image?.toString(),
                )
                viewModel.addProduct(p, inputParam)

                showAddEditDialog = false
            },
            onEdit = { updated ->
                viewModel.editProduct(updated, inputParam)
                showAddEditDialog = false
            },
            onDelete = {
                editProduct?.let { viewModel.deleteProduct(it, inputParam) }
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }
}

@Composable
fun ProductContent(
    products: List<Product>,
    modifier: Modifier = Modifier,
    onGoToDetail: ((Product) -> Unit)? = null,
    onEdit: (Product) -> Unit,
) {
    if (products.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.empty_products))
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(products, key = { it.productId }) { product ->
            ProductTile(
                product = product,
                onClick = { onGoToDetail?.invoke(product) },
                onEdit = { onEdit(product) },
            )
        }
    }
}

@Composable
private fun ProductTile(
    product: Product,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onEdit() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(modifier = Modifier.padding(8.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LargeImage(product.image)
                }
                Spacer(space = 8)
                Title(product.title)
                Spacer(space = 4)
                product.description?.let {
                    Subtitle(
                        text = it,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddOrEditProductDialog(
    product: Product? = null,
    onAdd: (String, String, Uri?) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (product == null) stringResource(R.string.add_product) else stringResource(
                    R.string.edit
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) })
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) })
                SecondaryButton(
                    if (imageUri != null) stringResource(R.string.selected_image) else stringResource(
                        R.string.choose_image
                    )
                ) {
                    launcher.launch("image/*")
                }

                SmallImage(imageUri?.toString()?: (product?.image))

                SecondaryButton(
                    text = stringResource(R.string.delete)
                ) {
                    onDelete()
                }
            }
        },
        confirmButton = {
            PrimaryButton(if (product == null) stringResource(R.string.save) else stringResource(R.string.edit)) {
                if (product == null) {
                    onAdd(title, description, imageUri)
                } else {
                    onEdit(
                        product.copy(
                            title = title,
                            description = description.ifBlank { null },
                            image = imageUri?.toString() ?: product.image,
                        )
                    )
                }
            }
        },

        dismissButton = {
            Row {
                SecondaryButton(stringResource(R.string.cancel)) { onDismiss() }
            }
        }
    )
}

/* Previews */

@Preview(
    name = "ProductContent Phone Light",
    device = Devices.PIXEL_4,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "ProductContent Phone Dark",
    device = Devices.PIXEL_4,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProductContentPhonePreview() {
    MaterialTheme {
        ProductContent(
            products = sampleProducts(),
            onEdit = {},
        )
    }
}

@Preview(
    name = "ProductContent Tablet Light",
    device = Devices.PIXEL_C,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "ProductContent Tablet Dark",
    device = Devices.PIXEL_C,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProductContentTabletPreview() {
    MaterialTheme {
        ProductContent(
            products = sampleProducts(),
            onEdit = {},
        )
    }
}

@Preview(
    name = "ProductContent Empty Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "ProductContent Empty Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProductContentEmptyPreview() {
    MaterialTheme {
        ProductContent(
            products = emptyList(),
            onEdit = {},
        )
    }
}

private fun sampleProducts(): List<Product> = listOf(
    Product("1", "Product One", "Description for product one", null, null),
    Product(
        "2",
        "Product Two with a Very Long Title",
        "This is a much longer description that should wrap to multiple lines",
        null,
        null
    ),
    Product("3", "Product Three", "Short desc", null, null),
    Product("4", "Product Four", null, null)
)


@Preview(name = "AddOrEditProductDialog", showBackground = true)
@Composable
private fun AddOrEditProductDialogPreview() {
    AppTheme {
        AddOrEditProductDialog(
            product = Product(
                productId = "preview",
                title = "Preview Product",
                description = "Short preview description",
                image = null
            ),
            onAdd = { _, _, _ -> },
            onEdit = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}

