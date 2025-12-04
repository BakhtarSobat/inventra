package com.bsobat.inventra.ui.checkout

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.data.model.PaymentMethod
import com.bsobat.inventra.ui.components.Divider
import com.bsobat.inventra.ui.components.Error
import com.bsobat.inventra.ui.components.FullScreenLoading
import com.bsobat.inventra.ui.components.PinDialog
import com.bsobat.inventra.ui.components.PrimaryButton
import com.bsobat.inventra.ui.components.SecondaryButton
import com.bsobat.inventra.ui.components.Text
import com.bsobat.inventra.ui.components.Title
import com.bsobat.inventra.ui.ext.formatPricePerUom
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import com.bsobat.inventra.ui.components.showToast

@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    onCheckoutComplete: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel: CheckoutViewModel = koinViewModel()

    val uiState by viewModel.load().collectAsStateWithLifecycle()
    val paymentMethodUiState by viewModel.paymentMethodState.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    val adminRight by viewModel.adminLoggedIn.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CheckoutEffect.CheckoutSuccess -> {
                    onCheckoutComplete(effect.saleId)
                }

                is CheckoutEffect.CheckoutError -> {
                    // Error is shown in the UI
                }

                is CheckoutEffect.CheckoutOnlinePaymentRequired -> {

                    val intent = Intent(Intent.ACTION_VIEW, effect.paymentUrl.toUri())
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        showToast(context, context.getString(R.string.error_no_app_found))
                    }
                    onCheckoutComplete(effect.saleId)
                }

            }
        }
    }



    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (val state = uiState) {
            is CheckoutUiState.Loading -> FullScreenLoading()

            is CheckoutUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = onNavigateBack,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is CheckoutUiState.Success -> {
                editPaymentMethod = state.selectedPaymentMethod
                CheckoutContent(
                    state = state,
                    paymentMethodUiState = paymentMethodUiState,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier.fillMaxSize(),
                    onEdit = { payment ->
                        editPaymentMethod = payment
                        showPinDialog = true
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(bottom = 54.dp)) {
        FloatingActionButton(
            onClick = {
                editPaymentMethod = null
                showPinDialog = true
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Payments, contentDescription = "Add Payment Method")
        }
    }

    if (showPinDialog) {
        if (adminRight) {
            showPinDialog = false
            showEditDialog = true
        } else {
            when(editPaymentMethod){
                is PaymentMethod.Cash -> Unit
                null, is PaymentMethod.Online -> {
                    PinDialog(
                        onPinEntered = { pin -> viewModel.onAdminPinCheck(pin) },
                        onDismiss = { showPinDialog = false },
                        pinError = false
                    )
                }
            }

        }
    }

    if (showEditDialog) {
        when(editPaymentMethod){
            is PaymentMethod.Cash -> Unit
            null , is PaymentMethod.Online -> {
                EditOnlinePaymentMethod(
                    payment = editPaymentMethod as PaymentMethod.Online?,
                    onEdit = { payment ->
                        viewModel.savePaymentMethod(payment)
                        showEditDialog = false
                        showPinDialog = false
                    },
                    onDismiss = {
                        showEditDialog = false
                    },
                    onAdd = { payment ->
                        viewModel.savePaymentMethod(payment)
                        showEditDialog = false
                        showPinDialog = false
                    },
                    onDelete = { payment ->
                        viewModel.deletePaymentMethod(payment)
                        showEditDialog = false
                        showPinDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    state: CheckoutUiState.Success,
    paymentMethodUiState: PaymentMethodUiState,
    onEvent: (CheckoutEvent) -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (PaymentMethod) -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Order Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Title(text = stringResource(R.string.checkout_order_summary))
                Spacer(modifier = Modifier.height(8.dp))

                SummaryRow(label = stringResource(R.string.subtotal), amount = state.subtotal)
                SummaryRow(label = stringResource(R.string.tax), amount = state.tax)
                Divider()
                SummaryRow(
                    label = stringResource(R.string.total),
                    amount = state.total,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Methods
        Title(text = stringResource(R.string.select_payment_method))

        Spacer(modifier = Modifier.height(8.dp))

        PaymentMethodContent(paymentMethodUiState, state, onEvent, onEdit)

        // Error Message
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Error(text = error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pay Button
        PrimaryButton(
            text = state.total.formatPricePerUom(),
            click = { onEvent(CheckoutEvent.ProcessCheckout) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.selectedPaymentMethod != null &&
                    !state.isProcessing &&
                    state.basketItems.isNotEmpty()
        )
    }
}

@Composable
private fun ColumnScope.PaymentMethodContent(
    paymentMethodUiState: PaymentMethodUiState,
    state: CheckoutUiState.Success,
    onEvent: (CheckoutEvent) -> Unit,
    onEdit: (PaymentMethod) -> Unit
) {
    if (paymentMethodUiState is PaymentMethodUiState.Success) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = paymentMethodUiState.paymentMethods,
                key = { it.id }
            ) { method ->
                PaymentMethodCard(
                    paymentMethod = method,
                    isSelected = state.selectedPaymentMethod?.id == method.id,
                    onSelect = { onEvent(CheckoutEvent.SelectPaymentMethod(method)) },
                    onEdit = onEdit
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        SecondaryButton(text = stringResource(R.string.back), click = onRetry)
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = style,
        )
        Text(
            text = amount.formatPricePerUom(),
            style = style,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodCard(
    paymentMethod: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (PaymentMethod) -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onSelect() },
                        onLongPress = { onEdit(paymentMethod) }
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = paymentMethod.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = paymentMethod.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Unselected",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EditOnlinePaymentMethod(
    payment: PaymentMethod.Online?,
    onEdit: (PaymentMethod) -> Unit,
    onAdd: (PaymentMethod) -> Unit,
    onDelete: (PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(payment?.config?.name ?: "") }
    var baseUrl by remember { mutableStateOf(payment?.config?.baseUrl ?: "") }
    var description by remember { mutableStateOf(payment?.config?.description ?: "") }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(payment?.name?:"Online")
        },
        text = {
            Column (
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        payment?.let { product ->
                            product.config = PaymentMethod.Online.Config(
                                name = name,
                                baseUrl = baseUrl,
                                description = description
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    label = { Text(stringResource(R.string.title)) }
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        payment?.let { product ->
                            product.config = PaymentMethod.Online.Config(
                                name = name,
                                baseUrl = baseUrl,
                                description = description
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    label = { Text(stringResource(R.string.url)) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        payment?.let { product ->
                            product.config = PaymentMethod.Online.Config(
                                name = name,
                                baseUrl = baseUrl,
                                description = description
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.description)) }
                )
                payment?.let {
                    SecondaryButton(stringResource(R.string.delete)) { onDelete(payment) }
                }
            }

        },
        confirmButton = {
            if(payment != null) {
                PrimaryButton(stringResource(R.string.save)) {
                    onEdit(payment)
                }
            } else {
                PrimaryButton(stringResource(R.string.save)) {
                    onAdd(
                        PaymentMethod.Online(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            enabled = true,
                            config = PaymentMethod.Online.Config(
                                name = name,
                                baseUrl = baseUrl,
                                description = description
                            )
                        )
                    )
                }
            }
        },
        dismissButton = {
            SecondaryButton(stringResource(R.string.cancel)) { onDismiss() }
        }
    )
}


@Composable
@Preview(name = "CheckoutContent - Empty Basket")
fun PreviewCheckoutContentEmptyBasket() {
    CheckoutContent(
        state = CheckoutUiState.Success(
            basketItems = emptyList(),
            subtotal = 0.0,
            tax = 0.0,
            total = 0.0,
            selectedPaymentMethod = null,
            isProcessing = false
        ),
        paymentMethodUiState = PaymentMethodUiState.Success(
            paymentMethods = emptyList(),
        ),
        onEvent = {},
        onEdit = {}
    )
}

@Composable
@Preview(name = "CheckoutContent - With Items, No Payment Selected")
fun PreviewCheckoutContentWithItemsNoPayment() {
    CheckoutContent(
        state = CheckoutUiState.Success(
            basketItems = listOf(
                createBasketItem(name = "Item 1", price = 10.0),
                createBasketItem(name = "Item 2", price = 20.0)
            ),
            subtotal = 30.0,
            tax = 3.0,
            total = 33.0,
            selectedPaymentMethod = null,
            isProcessing = false
        ),
        paymentMethodUiState = PaymentMethodUiState.Success(
            paymentMethods = listOf(
                PaymentMethod.Online("1", "Online"),
                PaymentMethod.Cash( "2", "Cash")
            ),
        ),
        onEvent = {},
        onEdit = {}
    )
}

@Composable
@Preview(name = "CheckoutContent - With Items, Payment Selected, Processing")
fun PreviewCheckoutContentWithItemsPaymentSelectedProcessing() {
    CheckoutContent(
        state = CheckoutUiState.Success(
            basketItems = listOf(
                createBasketItem(name = "Item 1", price = 10.0),
                createBasketItem(name = "Item 2", price = 20.0)
            ),
            subtotal = 30.0,
            tax = 3.0,
            total = 33.0,
            selectedPaymentMethod = PaymentMethod.Online(
                id = "1",
                name = "Online"
            ),
            isProcessing = true
        ),
        paymentMethodUiState = PaymentMethodUiState.Success(
            paymentMethods = listOf(
                PaymentMethod.Online(
                    id = "1",
                    name = "Online"
                ),
                PaymentMethod.Cash(
                    id = "2",
                    name = "Cash"
                )
            ),
        ),
        onEvent = {},
        onEdit = {},
    )
}

private fun createBasketItem(
    name: String,
    price: Double
): BasketItem {
    val offer = Offer(
        offerId = name,
        title = name,
        price = price,
        image = null,
        productId = "pid",
        amountInInventory = 10,
        type = "",
        uom = "g",
        taxPercentage = 5.0
    )
    return BasketItem(offer = offer, quantity = 1)
}

