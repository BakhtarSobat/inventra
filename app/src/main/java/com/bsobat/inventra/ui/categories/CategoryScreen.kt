package com.bsobat.inventra.ui.categories

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.Category
import com.bsobat.inventra.theme.AppTheme
import com.bsobat.inventra.ui.components.Divider
import com.bsobat.inventra.ui.components.FullScreenLoading
import com.bsobat.inventra.ui.components.LargeImage
import com.bsobat.inventra.ui.components.PinDialog
import com.bsobat.inventra.ui.components.PrimaryButton
import com.bsobat.inventra.ui.components.SecondaryButton
import com.bsobat.inventra.ui.components.SmallImage
import com.bsobat.inventra.ui.components.Spacer
import com.bsobat.inventra.ui.components.Subtext
import com.bsobat.inventra.ui.components.Text
import com.bsobat.inventra.ui.components.Title
import org.koin.androidx.compose.koinViewModel

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    onGoToDetail: ((Category) -> Unit),
) {
    val viewModel: CategoryViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsState().value
    var editCategory by remember { mutableStateOf<Category?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    val adminRight = viewModel.adminLoggedIn.collectAsState().value

    when (state) {
        is CategoryUiState.Success -> {
            CategoryContent(
                modifier = modifier,
                onGoToDetail = onGoToDetail,
                state.categories,
                onEdit = { category ->
                    editCategory = category
                    showPinDialog = true
                }
            )
        }

        is CategoryUiState.Loading -> {
            FullScreenLoading()
        }

        is CategoryUiState.Error -> {

        }
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = {
                editCategory = null
                showPinDialog = true
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Category")
        }
    }



    if (showPinDialog) {
        if (adminRight) {
            showPinDialog = false
            showAddDialog = true
        } else {
            PinDialog(
                onPinEntered = { pin ->
                    viewModel.onAdminPinCheck(pin)
                },
                onDismiss = { showPinDialog = false; pinError = false },
                pinError = pinError
            )
        }
    }
    if (showAddDialog) {
        AddOrEditCategoryDialog(
            category = editCategory,
            onAdd = { title, description, image, tax ->
                viewModel.createCategory(title, description, image, tax)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
            onEdit = { category ->
                showAddDialog = false
                viewModel.editCategory(category)
            },
            onDelete = {category -> viewModel.deleteCategory(category)}
        )
    }

}

@Composable
fun CategoryContent(
    modifier: Modifier = Modifier,
    onGoToDetail: ((Category) -> Unit),
    categories: List<Category>,
    onEdit: (Category) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Spacer()
        Title(stringResource(R.string.categories))
        Spacer()
        Divider()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                categories.size, key = { it }
            ) { item ->
                Category(
                    category = categories[item],
                    onGoToDetail = onGoToDetail,
                    onEdit = onEdit
                )
            }

        }
    }
}

@Composable
fun Category(
    category: Category,
    onGoToDetail: ((Category) -> Unit),
    onEdit: (Category) -> Unit
) {
    var onLongPress by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoToDetail.invoke(category) }
            .padding(vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress = true
                    },
                    onTap = {
                        onGoToDetail.invoke(category)
                    }
                )
            }
    ) {
        if (onLongPress) {
            onEdit(category)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {

            SmallImage(url = category.image)

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Title(
                    text = category.title,
                )
                if (!category.description.isNullOrBlank()) {
                    Subtext(
                        text = category.description ?: "",
                    )
                }
            }
        }
    }
}

@Composable
fun AddOrEditCategoryDialog(
    category: Category? = null,
    onAdd: (String, String?, String?, Double) -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(category?.title ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var imageUri by remember { mutableStateOf(category?.image?.toUri()) }
    var tax by remember { mutableStateOf(category?.taxPercentage?.toString() ?: "") }
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (category == null) stringResource(R.string.add_categories) else stringResource(
                    R.string.edit_category
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
                LargeImage(imageUri.toString())
                OutlinedTextField(
                    value = tax,
                    onValueChange = { tax = it },
                    label = { Text(stringResource(R.string.tax)) })
                category?.let{cat ->
                    Divider()
                    SecondaryButton(stringResource(R.string.delete)) {
                        onDelete(cat)
                    }
                }

            }
        },
        confirmButton = {
            PrimaryButton(if (category == null) stringResource(R.string.save) else stringResource(R.string.edit)) {
                val taxValue = tax.toDoubleOrNull() ?: 0.0
                if (category != null) {
                    val updatedCategory = category.copy(
                        title = title,
                        description = description.ifBlank { null },
                        image = imageUri?.toString(),
                        taxPercentage = taxValue
                    )
                    onEdit(updatedCategory)
                } else {
                    onAdd(title, description.ifBlank { null }, imageUri?.toString(), taxValue)
                }

            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryPreview() {
    Category(
        category = Category(
            categoryId = "1",
            title = "Electronics",
            description = "Devices and gadgets",
            image = null, // or provide a sample image URI
            taxPercentage = 18.0
        ),
        onGoToDetail = {},
        onEdit = {}
    )
}

@Preview(
    name = "CategoryContent Light",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun CategoryContentLightPreview() {
    AppTheme {
        CategoryContent(
            categories = listOf(
                Category("1", "Electronics", "Devices and gadgets", null, 18.0),
                Category("2", "Books", "Reading material", null, 5.0)
            ),
            onGoToDetail = {},
            onEdit = {},
        )
    }
}

@Preview(
    name = "CategoryContent Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CategoryContentDarkPreview() {
    AppTheme {
        CategoryContent(
            categories = listOf(
                Category("1", "Electronics", "Devices and gadgets", null, 18.0),
                Category("2", "Books", "Reading material", null, 5.0)
            ),
            onGoToDetail = {},
            onEdit = {}
        )
    }
}