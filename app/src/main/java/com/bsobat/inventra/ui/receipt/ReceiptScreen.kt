package com.bsobat.inventra.ui.receipt

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.PaymentPart
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.data.model.SaleItem
import com.bsobat.inventra.theme.AppTheme
import com.bsobat.inventra.ui.components.Divider
import com.bsobat.inventra.ui.components.Error
import com.bsobat.inventra.ui.components.FullScreenLoading
import com.bsobat.inventra.ui.components.SmallImage
import com.bsobat.inventra.ui.components.Text
import com.bsobat.inventra.ui.components.Title
import com.bsobat.inventra.ui.ext.formatPricePerUom
import com.bsobat.inventra.ui.ext.openPdf
import com.bsobat.inventra.ui.ext.toFormattedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    saleId: String,
    modifier: Modifier = Modifier,
    completed: () -> Unit
) {
    val viewModel: ReceiptViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val configData by viewModel.configData.collectAsState(initial = ConfigData(null, null, null, null))
    val context = LocalContext.current
    val pdfExportState by viewModel.pdfExportState.collectAsState()
    var printIsReady by remember { mutableStateOf(false) }

    LaunchedEffect(saleId) {
        viewModel.loadSale(saleId)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is ReceiptUiState.Loading -> {
                FullScreenLoading()
            }
            is ReceiptUiState.Error -> {
                Error(state.message)
            }
            is ReceiptUiState.Success -> {
                ReceiptContent(
                    sale = state.sale,
                    configData = configData,
                    onExportPdf = {
                        viewModel.exportToPdf()
                        printIsReady = true
                    },
                    modifier = modifier
                )
            }
        }
    }

    if(printIsReady) {
        HandlePdfExportState(context, pdfExportState) {
            completed()
            printIsReady = false
        }
    }
}
@Composable
fun HandlePdfExportState(
    context: Context,
    pdfExportState: PdfExportState,
    onCompleted: () -> Unit
) {
    when (pdfExportState) {
        is PdfExportState.Idle -> {
            // Do nothing
        }

        is PdfExportState.Exporting -> {
            // Show loading indicator if needed
        }

        is PdfExportState.Success -> {
            LaunchedEffect(Unit) {
                openPdf(context, pdfExportState.filePath)
                onCompleted()
            }
        }

        is PdfExportState.Error -> {
            LaunchedEffect(Unit) {
                // Show error message
                onCompleted()
            }
        }
    }
}

@Composable
fun ReceiptContent(
    sale: Sale,
    configData: ConfigData,
    onExportPdf: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Title(text = stringResource(R.string.receipt))

            Spacer(modifier = Modifier.height(24.dp))

            // Company Information Header
            configData.companyName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            configData.companyLogo?.let { logo ->
                // Display logo if needed - you may need to implement image loading
                Box( modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    SmallImage(logo)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            configData.companyDescription?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            configData.eventName?.let { eventName ->
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (configData.companyName != null || configData.companyLogo != null ||
                configData.companyDescription != null || configData.eventName != null) {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
            }


            // Sale Details
            ReceiptRow(label = stringResource(R.string.date_time), value = sale.timestamp.toFormattedDateTime())
            ReceiptRow(label = stringResource(R.string.sale_id), value = sale.saleId)
            sale.eventId?.let {
                ReceiptRow(label = stringResource(R.string.event), value = it)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Items Section
            Title(text = stringResource(R.string.items))

            Spacer(modifier = Modifier.height(12.dp))

            sale.items.forEach { item ->
                SaleItemRow(item = item)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Totals
            val subtotal = sale.items.sumOf { it.totalPrice }
            val totalTax = sale.items.sumOf { it.totalPrice * (it.taxPercentage / 100) }
            val total = subtotal + totalTax

            ReceiptRow(
                label = stringResource(R.string.subtotal),
                value = subtotal.formatPricePerUom(),
            )
            if(totalTax > 0.0) {
                ReceiptRow(
                    label = stringResource(R.string.tax),
                    value = totalTax.formatPricePerUom(),
                )
            }
            ReceiptRow(
                label = stringResource(R.string.total),
                value = total.formatPricePerUom(),
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Payment Section
            Title(
                text = stringResource(R.string.payments)
            )

            Spacer(modifier = Modifier.height(12.dp))

            sale.payments.forEach { payment ->
                PaymentPartRow(payment = payment)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.thank_you_for_your_purchase),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        FloatingActionButton(
            onClick = {
                onExportPdf()
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Print, contentDescription = stringResource(R.string.add_product))
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label
        )
        Text(
            text = value
        )
    }
}

@Composable
fun SaleItemRow(item: SaleItem) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        item.description?.let { description ->
            Text(
                text = description
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${item.quantity} " + item.unitPrice.formatPricePerUom(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.totalPrice.formatPricePerUom(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun PaymentPartRow(payment: PaymentPart) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = payment.method,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = payment.amount.formatPricePerUom()
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewReceiptContent() {
    AppTheme {
        ReceiptContent(
            sale = Sale(
                saleId = "SALE-2024-001",
                eventId = "EVENT-123",
                timestamp = "2024-01-15 14:30:00",
                items = listOf(
                    SaleItem(
                        offerId = "OFFER-001",
                        description = "Coffee - Large",
                        quantity = 2,
                        unitPrice = 4.50,
                        totalPrice = 9.00,
                        taxPercentage = 10.0,
                        inventoryAdjusted = true
                    ),
                    SaleItem(
                        offerId = "OFFER-002",
                        description = "Croissant",
                        quantity = 1,
                        unitPrice = 3.50,
                        totalPrice = 3.50,
                        taxPercentage = 0.0,
                        inventoryAdjusted = true
                    )
                ),
                payments = listOf(
                    PaymentPart(
                        method = "Cash",
                        amount = 10.00,
                        status = "COMPLETED"
                    ),
                    PaymentPart(
                        status = "COMPLETED",
                        method = "Card",
                        amount = 2.50
                    )
                )
            ),
            onExportPdf = {},
            modifier = Modifier,
            configData = ConfigData(
                companyName = "Cafe Delight",
                companyLogo = "https://1000logos.net/wp-content/uploads/2016/10/Amazon-logo-meaning.jpg",
                companyDescription = "The best coffee in town!",
                eventName = "Morning Specials"
            )
        )
    }
}
