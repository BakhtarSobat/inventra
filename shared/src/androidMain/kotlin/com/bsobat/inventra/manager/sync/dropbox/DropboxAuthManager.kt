package com.bsobat.inventra.manager.sync.dropbox

import android.content.Context
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.repository.ConfigurationRepository
import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.PlatformSyncDependencies
import com.bsobat.inventra.manager.sync.CloudSyncManager
import com.dropbox.core.android.Auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

actual class DropboxAuthManager actual constructor(
    private val contextProvider: ContextProvider,
    private val dataExportImportManager: DataExportImportManager,
    private val platformDependencies: PlatformSyncDependencies,
    private val configRepo: ConfigurationRepository
) {
    private val context: Context = contextProvider() as Context

    actual fun startSignIn(): Any? {
        val appKey = runBlocking {
            configRepo.config(DropboxConfigKeys.APP_KEY).first()
        } ?: throw IllegalStateException("Dropbox App Key not configured")

        Auth.startOAuth2Authentication(context, appKey)
        return null
    }

    actual fun handleSignInResult(): Result<DropboxCredentials> {
        return try {
            val accessToken = Auth.getOAuth2Token()
                ?: return Result.failure(Exception("No access token received"))

            val credentials = DropboxCredentials(
                accessToken = accessToken,
                accountEmail = "",
                displayName = ""
            )

            // Save credentials to ConfigurationRepository
            runBlocking {
                configRepo.upsert(DropboxConfigKeys.ACCESS_TOKEN, accessToken)
            }

            Result.success(credentials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual fun getLastSignedInAccount(): DropboxCredentials? {
        return runBlocking {
            val accessToken = configRepo.config(DropboxConfigKeys.ACCESS_TOKEN).first()
                ?: return@runBlocking null

            DropboxCredentials(
                accessToken = accessToken,
                accountEmail = configRepo.config(DropboxConfigKeys.ACCOUNT_EMAIL).first() ?: "",
                displayName = configRepo.config(DropboxConfigKeys.DISPLAY_NAME).first() ?: ""
            )
        }
    }

    actual suspend fun signOut() {
        configRepo.remove(DropboxConfigKeys.ACCESS_TOKEN)
        configRepo.remove(DropboxConfigKeys.ACCOUNT_EMAIL)
        configRepo.remove(DropboxConfigKeys.DISPLAY_NAME)
    }

    actual fun getCloudSyncManager(): CloudSyncManager? {
        val credentials = getLastSignedInAccount() ?: return null
        val provider = DropboxProvider(credentials)
        return CloudSyncManager(dataExportImportManager, provider, platformDependencies)
    }
}