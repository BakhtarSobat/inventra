package com.bsobat.inventra.ui


import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsobat.inventra.basket.usecase.ObserveBasketItemsUseCase
import com.bsobat.inventra.domain.usecase.AdminPinCheckUseCase
import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.sync.ConflictResolution
import com.bsobat.inventra.manager.sync.SyncAction
import com.bsobat.inventra.manager.sync.SyncResult
import com.bsobat.inventra.manager.sync.drive.GoogleDriveAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
    data class Conflict(val localTimestamp: String, val remoteTimestamp: String) : SyncState()
}
class MainActivityViewModel(
    private val dataExportImportManager: DataExportImportManager,
    observeBasketItemsUseCase: ObserveBasketItemsUseCase,
    private val adminPinCheckUseCase: AdminPinCheckUseCase,
    private val authManager: GoogleDriveAuthManager

) : ViewModel() {
    val adminLoggedIn = adminPinCheckUseCase.adminLoggedIn
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val basketItemsFlow = observeBasketItemsUseCase()
    suspend fun export(path: String) = dataExportImportManager.exportToZip(path)
    suspend fun import(path: String) {
        dataExportImportManager.importFromZip(path).onFailure {
            Log.e("Import", "import failed", it)
        }
    }

    fun onAdminPinCheck(pin: String) {
        viewModelScope.launch {
            adminPinCheckUseCase(pin)
        }
    }

    fun signIn() {
        authManager.startSignIn()
    }

    fun handleSignInResult(resultData: Any?) {
        authManager.handleSignInResult(resultData)
            .onSuccess { credentials ->
                // Handle success
            }
            .onFailure { error ->
                // Handle error
            }
    }

    fun signInToGoogleDrive(): Intent? {
        return authManager.startSignIn() as? Intent
    }

    fun handleGoogleDriveSignInResult(resultData: Any?) {
        authManager.handleSignInResult(resultData)
            .onSuccess { credentials ->
                _syncState.value = SyncState.Success("Signed in as ${credentials.displayName}")
            }
            .onFailure { error ->
                _syncState.value = SyncState.Error("Sign-in failed: ${error.message}")
            }
    }

    fun signOutFromGoogleDrive() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                _syncState.value = SyncState.Success("Signed out successfully")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Sign-out failed: ${e.message}")
            }
        }
    }

    fun syncWithGoogleDrive(conflictResolution: ConflictResolution = ConflictResolution.NEWEST_WINS) {
        viewModelScope.launch {
            val cloudSyncManager = authManager.getCloudSynManager()

            if (cloudSyncManager == null) {
                _syncState.value = SyncState.Error("Not signed in to Google Drive")
                return@launch
            }

            _syncState.value = SyncState.Loading

            when (val result = cloudSyncManager.syncWithCloud(conflictResolution)) {
                is SyncResult.Success -> {
                    val message = when (result.action) {
                        SyncAction.UPLOADED -> "Backup uploaded successfully"
                        SyncAction.DOWNLOADED -> "Backup downloaded successfully"
                        SyncAction.NO_CHANGE -> "Already up to date"
                        SyncAction.CONFLICT -> "Conflict detected"
                    }
                    _syncState.value = SyncState.Success(message)
                }
                is SyncResult.Conflict -> {
                    _syncState.value = SyncState.Conflict(result.localTimestamp, result.remoteTimestamp)
                }
                is SyncResult.Error -> {
                    _syncState.value = SyncState.Error(result.message)
                }
            }
        }
    }

    fun checkGoogleDriveSignInStatus(): Boolean {
        return authManager.getLastSignedInAccount() != null
    }


}