package com.bsobat.inventra.manager.sync.dropbox

import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.repository.ConfigurationRepository
import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.PlatformSyncDependencies
import com.bsobat.inventra.manager.sync.CloudSyncManager

expect class DropboxAuthManager(
    contextProvider: ContextProvider,
    dataExportImportManager: DataExportImportManager,
    platformDependencies: PlatformSyncDependencies,
    configRepo: ConfigurationRepository
) {
    fun startSignIn(): Any?
    fun handleSignInResult(): Result<DropboxCredentials>
    fun getLastSignedInAccount(): DropboxCredentials?
    suspend fun signOut()
    fun getCloudSyncManager(): CloudSyncManager?
}
