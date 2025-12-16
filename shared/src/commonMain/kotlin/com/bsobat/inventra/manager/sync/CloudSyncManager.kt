package com.bsobat.inventra.manager.sync

import com.bsobat.inventra.manager.DataExportImportManager
import com.bsobat.inventra.manager.PlatformSyncDependencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


sealed class SyncResult {
    data class Success(val action: SyncAction) : SyncResult()
    data class Conflict(val localTimestamp: String, val remoteTimestamp: String) : SyncResult()
    data class Error(val message: String, val exception: Throwable? = null) : SyncResult()
}

enum class SyncAction {
    UPLOADED,
    DOWNLOADED,
    NO_CHANGE,
    CONFLICT
}

enum class ConflictResolution {
    NEWEST_WINS,
    LOCAL_WINS,
    REMOTE_WINS,
    ASK_USER
}


class CloudSyncManager(
    private val dataExportImportManager: DataExportImportManager,
    private val cloudProvider: CloudStorageProvider,
    platformDependencies: PlatformSyncDependencies,
) {
    private val appFilesDir: String = platformDependencies.getAppFilesDir()
    private val deviceId: String = platformDependencies.getDeviceId()
    private val json = Json { prettyPrint = true }
    private val fileSystem = FileSystem.SYSTEM

    private val syncMetadataFile = appFilesDir.toPath().resolve("sync_metadata.json")
    private val remoteSyncFileName = "inventra_backup.zip"
    private val remoteMetadataFileName = "inventra_backup_metadata.json"

    @OptIn(ExperimentalTime::class)
    suspend fun syncWithCloud(conflictResolution: ConflictResolution = ConflictResolution.NEWEST_WINS): SyncResult =
        withContext(Dispatchers.Default) {
            try {
                val localMetadata = loadLocalSyncMetadata()
                val remoteExists = cloudProvider.fileExists(remoteMetadataFileName)

                if (!remoteExists) {
                    // No remote backup, upload local
                    return@withContext uploadToCloud()
                }

                val remoteMetadata = downloadRemoteMetadata()

                // Compare timestamps
                val comparison = compareTimestamps(localMetadata, remoteMetadata)

                when {
                    comparison > 0 -> {
                        // Local is newer, upload
                        uploadToCloud()
                    }

                    comparison < 0 -> {
                        // Remote is newer, download
                        downloadFromCloud()
                    }

                    else -> {
                        // Same timestamp
                        SyncResult.Success(SyncAction.NO_CHANGE)
                    }
                }
            } catch (e: Exception) {
                SyncResult.Error("Sync failed: ${e.message}", e)
            }
        }

    @OptIn(ExperimentalTime::class)
    suspend fun uploadToCloud(): SyncResult = withContext(Dispatchers.Default) {
        try {
            val tempZipPath = appFilesDir.toPath()
                .resolve("temp_export_${Clock.System.now().nanosecondsOfSecond}.zip")

            // Export data to zip
            dataExportImportManager.exportToZip(tempZipPath.toString()).getOrThrow()

            // Upload zip file
            cloudProvider.uploadFile(tempZipPath, remoteSyncFileName).getOrThrow()

            // Create and upload metadata
            val metadata = SyncMetadata(
                lastSyncTimestamp = Clock.System.now().toString(),
                exportTimestamp = Clock.System.now().toString(),
                deviceId = deviceId
            )

            val metadataPath = appFilesDir.toPath().resolve("temp_metadata.json")
            fileSystem.write(metadataPath) {
                writeUtf8(json.encodeToString(SyncMetadata.serializer(), metadata))
            }

            cloudProvider.uploadFile(metadataPath, remoteMetadataFileName).getOrThrow()

            // Save local sync metadata
            saveSyncMetadata(metadata)

            // Cleanup temp files
            fileSystem.delete(tempZipPath)
            fileSystem.delete(metadataPath)

            SyncResult.Success(SyncAction.UPLOADED)
        } catch (e: Exception) {
            SyncResult.Error("Upload failed: ${e.message}", e)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun downloadFromCloud(): SyncResult = withContext(Dispatchers.Default) {
        try {
            val tempZipPath = appFilesDir.toPath()
                .resolve("temp_import_${Clock.System.now().nanosecondsOfSecond}.zip")

            // Download zip file
            cloudProvider.downloadFile(remoteSyncFileName, tempZipPath).getOrThrow()

            // Import data from zip
            dataExportImportManager.importFromZip(tempZipPath.toString()).getOrThrow()

            // Download and save metadata
            val remoteMetadata = downloadRemoteMetadata()
            saveSyncMetadata(
                remoteMetadata.copy(
                    lastSyncTimestamp = Clock.System.now().toString()
                )
            )

            // Cleanup temp file
            fileSystem.delete(tempZipPath)

            SyncResult.Success(SyncAction.DOWNLOADED)
        } catch (e: Exception) {
            SyncResult.Error("Download failed: ${e.message}", e)
        }
    }

    private suspend fun downloadRemoteMetadata(): SyncMetadata {
        val tempMetadataPath = appFilesDir.toPath().resolve("temp_remote_metadata.json")

        cloudProvider.downloadFile(remoteMetadataFileName, tempMetadataPath).getOrThrow()

        val metadata = fileSystem.read(tempMetadataPath) {
            json.decodeFromString<SyncMetadata>(readUtf8())
        }

        fileSystem.delete(tempMetadataPath)

        return metadata
    }

    @OptIn(ExperimentalTime::class)
    private fun loadLocalSyncMetadata(): SyncMetadata {
        return if (fileSystem.exists(syncMetadataFile)) {
            fileSystem.read(syncMetadataFile) {
                json.decodeFromString(readUtf8())
            }
        } else {
            SyncMetadata(
                lastSyncTimestamp = "1970-01-01T00:00:00Z",
                exportTimestamp = "1970-01-01T00:00:00Z",
                deviceId = deviceId
            )
        }
    }

    private fun saveSyncMetadata(metadata: SyncMetadata) {
        fileSystem.write(syncMetadataFile) {
            writeUtf8(json.encodeToString(SyncMetadata.serializer(), metadata))
        }
    }

    private fun compareTimestamps(local: SyncMetadata?, remote: SyncMetadata?): Int {
        if (local == null && remote == null) return 0
        if (local == null) return -1
        if (remote == null) return 1

        return local.exportTimestamp.compareTo(remote.exportTimestamp)
    }

    suspend fun getLastSyncInfo(): SyncInfo {
        val localMetadata = loadLocalSyncMetadata()
        val hasRemote = cloudProvider.fileExists(remoteMetadataFileName)

        return SyncInfo(
            lastSyncTimestamp = localMetadata.lastSyncTimestamp,
            lastExportTimestamp = localMetadata.exportTimestamp,
            deviceId = localMetadata.deviceId,
            hasRemoteBackup = hasRemote
        )
    }
}