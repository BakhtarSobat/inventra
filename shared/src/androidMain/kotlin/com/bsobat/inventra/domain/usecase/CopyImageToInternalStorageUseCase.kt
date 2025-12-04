package com.bsobat.inventra.domain.usecase

import android.content.Context
import android.net.Uri
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.PathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun copyImageToInternalStorageUseCase(
    contextProvider: ContextProvider,
    pathProvider: PathProvider,
    fileName: String
): String? = withContext(Dispatchers.IO) {
    val context = contextProvider() as Context
    val uri = pathProvider() as Uri

    val internalDirPath = context.filesDir.absolutePath
    val uriFilePath = uri.path?.let { File(it).absolutePath }
    if (uriFilePath != null && uriFilePath.startsWith(internalDirPath)) {
        return@withContext uriFilePath
    }

    val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
    val file = File(context.filesDir, fileName)
    file.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    file.absolutePath
}
