package com.bsobat.inventra.ui.navigation

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bsobat.inventra.ui.basket.BasketScreen
import com.bsobat.inventra.ui.categories.CategoryScreen
import com.bsobat.inventra.ui.checkout.CheckoutScreen
import com.bsobat.inventra.ui.config.ConfigurationScreen
import com.bsobat.inventra.ui.offer.OffersScreen
import com.bsobat.inventra.ui.products.ProductScreen
import com.bsobat.inventra.ui.receipt.ReceiptScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface Screen: Parcelable{
    object CategoriesScreen : Screen
    data class ProductsScreen(val categoryId: String?, val categoryImage: String?) : Screen
    data class OffersScreen(val productId: String, val productName: String, val productImage: String?) : Screen
    object BasketScreen : Screen
    object CheckoutScreen : Screen
    data class ReceiptScreen(val saleId: String) : Screen
    object ConfigurationScreen : Screen
}



@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Navigation2(navigator: ThreePaneScaffoldNavigator<Screen>) {
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = { ListPane(scope, navigator) },
        extraPane = { ExtraPane(navigator, scope) },
        detailPane = { DetailPane(navigator, scope) }
    )

}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun ThreePaneScaffoldPaneScope.ListPane(
    scope: CoroutineScope,
    navigator: ThreePaneScaffoldNavigator<Screen>
) {
    AnimatedPane {
        CategoryScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceDim)
        ) { category ->
            scope.launch {
                navigator.navigateTo(
                    pane = ListDetailPaneScaffoldRole.Detail,
                    contentKey = Screen.ProductsScreen(category.categoryId, category.image)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun ThreePaneScaffoldPaneScope.ExtraPane(
    navigator: ThreePaneScaffoldNavigator<Screen>,
    scope: CoroutineScope
) {
    AnimatedPane {
        when (val destination = navigator.currentDestination?.contentKey) {
            else -> Unit
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun ThreePaneScaffoldPaneScope.DetailPane(
    navigator: ThreePaneScaffoldNavigator<Screen>,
    scope: CoroutineScope
) {
    AnimatedPane {
        when (val destination = navigator.currentDestination?.contentKey) {
            is Screen.ProductsScreen -> {
                ProductScreen(
                    inputParam = destination,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) { product ->
                    scope.launch {
                        navigator.navigateTo(
                            pane = ListDetailPaneScaffoldRole.Detail,
                            contentKey = Screen.OffersScreen(
                                product.productId,
                                productName = product.title,
                                productImage = product.image
                            )
                        )
                    }
                }
            }

            is Screen.OffersScreen -> {
                OffersScreen(
                    inputParams = destination,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }

            is Screen.BasketScreen -> {
                BasketScreen(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    scope.launch {
                        navigator.navigateTo(
                            pane = ListDetailPaneScaffoldRole.Detail,
                            contentKey = Screen.CheckoutScreen
                        )
                    }
                }
            }

            is Screen.CheckoutScreen -> {
                CheckoutScreen(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    onNavigateBack = {
                        scope.launch { navigator.navigateBack() }
                    },
                    onCheckoutComplete = {saleId->
                        scope.launch {
                            navigator.navigateTo(
                                pane = ListDetailPaneScaffoldRole.Detail,
                                contentKey = Screen.ReceiptScreen(saleId)
                            )
                        }
                    }
                )
            }

            is Screen.ReceiptScreen -> {
                val saleId = (navigator.currentDestination?.contentKey as Screen.ReceiptScreen).saleId
                ReceiptScreen(
                    saleId = saleId,
                    completed =  {
                        scope.launch { navigator.navigateBack() }
                    },
                )
            }

            is Screen.ConfigurationScreen -> {
                ConfigurationScreen()
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Yellow.copy(alpha = 0.4f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select a category")
                }
            }
        }
    }
}