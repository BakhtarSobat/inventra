package com.bsobat.inventra.ui.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.BasketItem
import com.bsobat.inventra.data.model.Offer
import com.bsobat.inventra.theme.AppTheme
import com.bsobat.inventra.ui.components.CenteredText
import com.bsobat.inventra.ui.components.Divider
import com.bsobat.inventra.ui.components.PrimaryButton
import com.bsobat.inventra.ui.components.SmallImage
import com.bsobat.inventra.ui.components.Text
import com.bsobat.inventra.ui.components.Title
import com.bsobat.inventra.ui.ext.formatPricePerUom
import org.koin.androidx.compose.koinViewModel

@Composable
fun BasketScreen(
    modifier: Modifier,
    onCheckout: () -> Unit
) {
    val viewModel: BasketViewModel = koinViewModel()

    val items by viewModel.items.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val tax by viewModel.tax.collectAsState()
    val total by viewModel.total.collectAsState()

    BasketContent(
        items = items,
        subtotal = subtotal,
        tax = tax, total,
        onCheckout = onCheckout,
        onChangeQuantity = { offer, qty ->
            viewModel.changeQuantity(offer.offerId, qty)
        },
        onRemove = { offer ->
            viewModel.remove(offer.offerId)
        },
        modifier = modifier
    )
}

@Composable
private fun BasketContent(
    items: List<BasketItem>,
    subtotal: Double,
    tax: Double,
    total: Double,
    onCheckout: () -> Unit,
    onChangeQuantity: (Offer, Int) -> Unit,
    onRemove: (Offer) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CenteredText(text = "Basket", textColor = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            CenteredText(text = stringResource(R.string.empty_basket))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    BasketItemRow(
                        item = item,
                        onQuantityChange = { qty ->
                            onChangeQuantity(item.offer, qty)
                        },
                        onRemove = {
                            onRemove(item.offer)
                        }
                    )
                    Divider()
                }
            }

            Spacer(Modifier.height(16.dp))
            SummaryRow(label = stringResource(R.string.subtotal), value = subtotal)
            SummaryRow(label = stringResource(R.string.tax), value = tax)
            SummaryRow(label = stringResource(R.string.total), value = total)
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = stringResource(R.string.checkout), click = onCheckout, modifier = Modifier.fillMaxWidth())
        }
    }
}


@Composable
fun BasketItemRow(
    item: BasketItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        SmallImage(url = item.offer.image)

        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Title(item.offer.title)
            Text("Unit: ${item.offer.price.formatPricePerUom()}")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) }) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.decrease)
                    )
                }
                Text(
                    "${item.quantity}",
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = { onQuantityChange(item.quantity + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.increase))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${(item.offer.price * item.quantity)}",
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(text = value.formatPricePerUom())
    }
}

@Preview(showBackground = true, name = "Empty Basket")
@Composable
fun BasketContentPreview_Empty() {
    AppTheme {
        BasketContent(
            items = emptyList(),
            subtotal = 0.0,
            tax = 0.0,
            total = 0.0,
            onCheckout = {},
            onChangeQuantity = { _, _ -> },
            onRemove = { _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Single Item Basket")
@Composable
fun BasketContentPreview_OneItem() {
    AppTheme {
        val offer = Offer(
            offerId = "1",
            title = "Sample Product",
            price = 10.0,
            image = null,
            productId = "pid",
            amountInInventory = 10,
            type = "",
            uom = "g",
            taxPercentage = 5.0
        )
        val item = BasketItem(offer = offer, quantity = 2)
        BasketContent(
            items = listOf(item),
            subtotal = 20.0,
            tax = 1.0,
            total = 21.0,
            onCheckout = {},
            onChangeQuantity = { _, _ -> },
            onRemove = { _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Many Items Basket")
@Composable
fun BasketContentPreview_ManyItems() {
    AppTheme {
        val items = List(12) { idx ->
            val offer = Offer(
                offerId = "$idx",
                productId = "pid",
                amountInInventory = 10,
                type = "",
                uom = "g",
                title = "Product $idx",
                price = 5.0 + idx,
                image = null,
                taxPercentage = 10.0
            )
            BasketItem(offer = offer, quantity = idx + 1)
        }
        BasketContent(
            items = items,
            subtotal = items.sumOf { it.offer.price * it.quantity },
            tax = items.sumOf { it.offer.price * it.quantity * 0.1 },
            total = items.sumOf { it.offer.price * it.quantity * 1.1 },
            onCheckout = {},
            onChangeQuantity = { _, _ -> },
            onRemove = { _ -> }
        )
    }
}
