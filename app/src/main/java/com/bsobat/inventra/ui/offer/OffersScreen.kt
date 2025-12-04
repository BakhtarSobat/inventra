package com.bsobat.inventra.ui.offer

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.theme.AppTheme
import com.bsobat.inventra.ui.components.Error
import com.bsobat.inventra.ui.components.FullScreenLoading
import com.bsobat.inventra.ui.components.PinDialog
import com.bsobat.inventra.ui.components.PrimaryButton
import com.bsobat.inventra.ui.components.SecondaryButton
import com.bsobat.inventra.ui.components.SmallImage
import com.bsobat.inventra.ui.components.Text
import com.bsobat.inventra.ui.components.showToast
import com.bsobat.inventra.ui.ext.formatPricePerUom
import com.bsobat.inventra.ui.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun OffersScreen(
    inputParams: Screen.OffersScreen,
    modifier: Modifier = Modifier,
) {
    val viewModel: OfferViewModel = koinViewModel()
    LaunchedEffect(inputParams) {
        viewModel.loadOffersByProduct(inputParams)
    }
    val state by viewModel.uiState.collectAsState()
    val adminRight by viewModel.adminLoggedIn.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editOffer by remember { mutableStateOf<Offer?>(null) }


    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            OffersUiState.Loading -> FullScreenLoading()
            is OffersUiState.Error -> {
                val e = state as OffersUiState.Error
                Error("Error: ${e.message}")
            }
            is OffersUiState.Success -> {
                val s = state as OffersUiState.Success
                OfferContent(
                    offers = s.offers,
                    modifier = modifier,
                    onAddToBasket = { offer, quantity ->
                        viewModel.addToBasket(offer, quantity)
                        showToast(context, context.resources.getString(R.string.added_to_basket))
                                    },
                    onEdit = { offer ->
                        editOffer = offer
                        showPinDialog = true
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = {
                editOffer = null
                showPinDialog = true
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_offer)
            )
        }
    }

    if (showPinDialog) {
        if (adminRight) {
            showPinDialog = false
            showAddEditDialog = true
        } else {
            PinDialog(
                onPinEntered = { pin -> viewModel.onAdminPinCheck(pin) },
                onDismiss = {
                    showPinDialog = false
                },
                pinError = pinError
            )
        }
    }

    if (showAddEditDialog) {
        AddOrEditOfferDialog(
            product = inputParams,
            offer = editOffer,
            onAdd = { offer ->
                viewModel.addOffer(offer, inputParams)
                showAddEditDialog = false
            },
            onEdit = { offer ->
                viewModel.updateOffer(offer, inputParams)
                showAddEditDialog = false
            },
            onDelete = { offer ->
                viewModel.deleteOffer(offer, inputParams)
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }
}


@Composable
fun OfferContent(
    offers: List<Offer>,
    modifier: Modifier = Modifier,
    onAddToBasket: (Offer, Int) -> Unit,
    onEdit: (Offer) -> Unit
) {
    if (offers.isEmpty()) {
        Box(
            modifier = modifier
                .padding(8.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_offers_available))
        }
        return
    }
    LazyColumn(
        modifier = modifier
            .padding(8.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = offers,
            key = { it.offerId }
        ) { offer ->
            OfferRow(
                offer = offer,
                onAddToBasket = onAddToBasket,
                onEdit = onEdit
            )
        }
    }
}

@Composable
private fun OfferRow(
    offer: Offer,
    onAddToBasket: (Offer, Int) -> Unit,
    onEdit: (Offer) -> Unit
) {
    var quantity by remember(offer.offerId) { mutableStateOf("1") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val qty = quantity.toIntOrNull()?.takeIf { it > 0 } ?: 1
                            onAddToBasket(offer, qty)
                        },
                        onLongPress = { onEdit(offer) }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallImage(offer.image)

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = offer.price.formatPricePerUom() + "/ ${offer.uom}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.type, offer.type),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.stock_info, offer.amountInInventory),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            if ( newValue.all { it.isDigit() }) {
                                quantity = newValue
                            }
                        },
                        label = { Text(stringResource(R.string.quantity)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(72.dp)
                    )
                }

            }
            Spacer(modifier = Modifier.width(16.dp))

        }
    }
}

@Composable
private fun AddOrEditOfferDialog(
    product: Screen.OffersScreen,
    offer: Offer?,
    onAdd: (Offer) -> Unit,
    onEdit: (Offer) -> Unit,
    onDelete: (Offer) -> Unit = {},
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(offer?.title ?: product.productName) }
    var price by remember { mutableStateOf(offer?.price?.toString() ?: "") }
    var uom by remember { mutableStateOf(offer?.uom ?: "") }
    var type by remember { mutableStateOf(offer?.type ?: "") }
    var stock by remember { mutableStateOf(offer?.amountInInventory?.toString() ?: "") }
    var imageUri by remember { mutableStateOf(offer?.image?.let(Uri::parse)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            androidx.compose.material3.Text(
                text = if (offer == null) stringResource(R.string.add_offer) else stringResource(R.string.edit)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { androidx.compose.material3.Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.price)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uom,
                    onValueChange = { uom = it },
                    label = { Text(stringResource(R.string.uom)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text(stringResource(R.string.type_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text(stringResource(R.string.stock_info_label)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(
                    text = if (imageUri != null) stringResource(R.string.selected_image) else stringResource(R.string.choose_image),
                    click = { launcher.launch("image/*") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SmallImage(imageUri?.toString())

                Spacer(modifier = Modifier.height(8.dp))
                offer?.let {
                    SecondaryButton(
                        text = stringResource(R.string.delete)
                    ) {
                        onDelete(offer)
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (offer == null) stringResource(R.string.save) else stringResource(R.string.edit)
            ) {
                val parsedPrice = price.toDoubleOrNull() ?: 0.0
                val parsedStock = stock.toIntOrNull() ?: 0
                val updated = Offer(
                    offerId = offer?.offerId ?: UUID.randomUUID().toString(),
                    productId = product.productId,
                    title = title,
                    price = parsedPrice,
                    uom = uom,
                    type = type,
                    amountInInventory = parsedStock,
                    image = imageUri?.toString()
                )
                if (offer == null) onAdd(updated) else onEdit(updated)
            }
        },
        dismissButton = {
            SecondaryButton(text = stringResource(R.string.cancel), click = onDismiss)
        }
    )
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

@Preview(
    name = "OfferContent Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Composable
private fun OfferContentLightPreview() {
    AppTheme {
        OfferContent(
            offers = previewOffers(),
            onAddToBasket = { _, _ -> },
            onEdit = {}
        )
    }
}

@Preview(
    name = "OfferContent Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun OfferContentDarkPreview() {
    AppTheme {
        OfferContent(
            offers = previewOffers(),
            onAddToBasket = { _, _ -> },
            onEdit = {}
        )
    }
}

private fun previewOffers() = listOf(
    Offer(
        offerId = "offer-1",
        productId = "product-1",
        title = "Starter Pack",
        price = 19.99,
        uom = "kg",
        type = "Retail",
        amountInInventory = 48,
        image = null
    ),
    Offer(
        offerId = "offer-2",
        productId = "product-1",
        title = "Wholesale Bundle",
        price = 149.50,
        uom = "crate",
        type = "Wholesale",
        amountInInventory = 12,
        image = null
    )
)

@Preview(name = "AddOrEditOfferDialog", showBackground = true)
@Composable
private fun AddOrEditOfferDialogPreview() {
    AppTheme {
        AddOrEditOfferDialog(
            product = Screen.OffersScreen(
                productId = "preview-product",
                productImage = null,
                productName = "test"
            ),
            offer = Offer(
                offerId = "preview-offer",
                productId = "preview-product",
                title = "Sample Offer",
                price = 42.5,
                uom = "kg",
                type = "Retail",
                amountInInventory = 10,
                image = null
            ),
            onAdd = {},
            onEdit = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}
