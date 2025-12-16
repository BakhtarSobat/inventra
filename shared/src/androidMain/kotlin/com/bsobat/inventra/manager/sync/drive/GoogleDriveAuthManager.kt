package com.bsobat.inventra.manager.sync.drive

import android.content.Context
import android.content.Intent
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.PlatformSyncDependencies
import com.bsobat.inventra.manager.sync.CloudStorageProvider
import com.bsobat.inventra.manager.sync.CloudSyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class GoogleDriveAuthManager actual constructor(
    contextProvider: ContextProvider,
    private val dataExportImportManager: DataExportImportManager,
    private val platformDependencies: PlatformSyncDependencies,
) {
    private val context: Context = contextProvider() as Context

    private var cloudSyncManager: CloudSyncManager? = null
    private val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()

    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(context, signInOptions)
    }

    actual fun startSignIn(): Any? {
        // This needs to be called from Activity/Fragment
        // Store the intent to be launched by the UI
        signInIntent = googleSignInClient.signInIntent
        return signInIntent
    }

    actual fun handleSignInResult(resultData: Any?): Result<GoogleDriveCredentials> {
        return try {
            val data = resultData as? Intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult()

            if (account != null) {
                createCloudSynManager()
                Result.success(createCredentials(account))
            } else {
                Result.failure(Exception("Sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual fun getLastSignedInAccount(): GoogleDriveCredentials? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.let { createCredentials(it) }
    }

    actual suspend fun signOut() {
        googleSignInClient.signOut()
    }

    private fun createCredentials(account: GoogleSignInAccount): GoogleDriveCredentials {
        return GoogleDriveCredentials(
            accountEmail = account.email ?: "",
            folderId = null,
            accessToken = account.idToken ?: "",
            refreshToken = null,
            displayName = account.displayName ?: ""
        )
    }

    private fun getGoogleDriveProvider(): GoogleDriveProvider? {
        return getLastSignedInAccount()?.let { credentials ->
            GoogleDriveProvider(
                credentials = credentials
            )
        }
    }

    private fun createCloudSynManager() {
        val driveProvider = getGoogleDriveProvider() ?: return

        cloudSyncManager =  CloudSyncManager(
            dataExportImportManager = dataExportImportManager,
            cloudProvider = driveProvider,
            platformDependencies = platformDependencies,
        )
    }

    actual fun getCloudSynManager(): CloudSyncManager? {
        return cloudSyncManager
    }

    companion object {
        var signInIntent: Intent? = null
            private set

    }
}
