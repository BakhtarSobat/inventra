package com.bsobat.inventra.manager

import com.bsobat.inventra.data.di.ContextProvider

expect class PlatformSyncDependencies(contextProvider: ContextProvider) {
    fun getAppFilesDir(): String
    fun getDeviceId(): String
}