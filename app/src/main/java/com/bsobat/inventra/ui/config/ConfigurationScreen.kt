package com.bsobat.inventra.ui.config

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsobat.inventra.R
import com.bsobat.inventra.ui.components.FullScreenLoading
import com.bsobat.inventra.ui.components.PrimaryButton
import com.bsobat.inventra.ui.components.SecondaryButton
import com.bsobat.inventra.ui.components.SmallImage
import com.bsobat.inventra.ui.components.Title
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConfigurationScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: ConfigurationViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editableData by viewModel.editableData.collectAsStateWithLifecycle()

    when (val state = uiState) {
        ConfigurationUiState.Loading -> LoadingContent(modifier)
        is ConfigurationUiState.Error -> ErrorContent(state.message, modifier)
        is ConfigurationUiState.Success -> ConfigurationContent(
            modifier = modifier,
            data = editableData,
            onCompanyNameChanged = viewModel::updateCompanyName,
            onCompanyLogoChanged = viewModel::updateCompanyLogo,
            onCompanyDescriptionChanged = viewModel::updateCompanyDescription,
            onEventNameChanged = viewModel::updateEventName,
            onSave = {
                viewModel.saveConfiguration()
            }

        )
    }
}


@Composable
private fun ConfigurationContent(
    data: ConfigurationData,
    onCompanyNameChanged: (String) -> Unit,
    onCompanyLogoChanged: (Uri?) -> Unit,
    onCompanyDescriptionChanged: (String) -> Unit,
    onEventNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(24.dp)
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onCompanyLogoChanged( uri) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ConfigTextField(
            label = stringResource(R.string.label_company_name),
            value = data.companyName,
            onValueChange = onCompanyNameChanged
        )
        Text(
            text = stringResource(R.string.label_company_logo)
        )
        Spacer(modifier = Modifier.height(8.dp))
        SmallImage(data.companyLogo)
        SecondaryButton(
            text = if (data.companyLogo?.isNotBlank() == true) stringResource(R.string.selected_image) else stringResource(R.string.choose_image),
            click = { launcher.launch("image/*") }
        )
        ConfigTextField(
            label = stringResource(R.string.label_company_description),
            value = data.companyDescription,
            onValueChange = onCompanyDescriptionChanged,
            singleLine = false
        )
        ConfigTextField(
            label = stringResource(R.string.label_event_name),
            value = data.eventName,
            onValueChange = onEventNameChanged
        )

        PrimaryButton(
            text = stringResource(R.string.save),
        ) {
            onSave()
        }
    }
}

@Composable
private fun ConfigTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        FullScreenLoading()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Title(
            text = message
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigurationContentPreview() {
    ConfigurationContent(
        data = ConfigurationData(
            companyName = "Inventra Inc.",
            companyLogo = "https://example.com/logo.png",
            companyDescription = "Retail POS system powering on-site events.",
            eventName = "Inventra Expo 2025"
        ),
        onCompanyNameChanged = {},
        onCompanyLogoChanged = {},
        onCompanyDescriptionChanged = {},
        onEventNameChanged = {},
        onSave = {}
    )
}