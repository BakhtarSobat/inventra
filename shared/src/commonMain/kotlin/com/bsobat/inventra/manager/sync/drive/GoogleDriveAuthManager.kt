package com.bsobat.inventra.manager.sync.drive

import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.PlatformSyncDependencies
import com.bsobat.inventra.manager.sync.CloudStorageProvider
import com.bsobat.inventra.manager.sync.CloudSyncManager
import com.bsobat.inventra.manager.sync.drive.GoogleDriveCredentials

expect class GoogleDriveAuthManager(
    contextProvider: ContextProvider,
    dataExportImportManager: DataExportImportManager,
    platformDependencies: PlatformSyncDependencies,
) {
    fun startSignIn(): Any?
    fun handleSignInResult(resultData: Any?): Result<GoogleDriveCredentials>
    fun getLastSignedInAccount(): GoogleDriveCredentials?
    suspend fun signOut()
    fun getCloudSynManager(): CloudSyncManager?
}