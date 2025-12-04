package com.bsobat.inventra.domain.usecase

import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.PathProvider

expect suspend fun copyImageToInternalStorageUseCase(
    contextProvider: ContextProvider,
    pathProvider: PathProvider,
    fileName: String
): String?
